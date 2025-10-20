package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogDTO;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.vehicle.Model;
import com.evdealer.evdealermanagement.entity.vehicle.ModelVersion;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleBrands;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleCatalog;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleCategories;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleDetails;
import com.evdealer.evdealermanagement.mapper.post.PostVerifyMapper;
import com.evdealer.evdealermanagement.mapper.vehicle.VehicleCatalogMapper;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.VehicleCatalogRepository;
import com.evdealer.evdealermanagement.repository.VehicleDetailsRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class StaffService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserContextService userContextService;
    @Autowired
    private PostPaymentRepository postPaymentRepository;
    @Autowired
    private VehicleCatalogRepository vehicleCatalogRepository;
    @Autowired
    private GeminiRestService geminiRestService;
    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Transactional
    public PostVerifyResponse verifyPost(String productId, PostVerifyRequest request) {

        // 1) Lấy user hiện tại (phải có quyền STAFF hoặc ADMIN)
        Account currentUser = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user"));

        if (currentUser.getRole() != Account.Role.STAFF && currentUser.getRole() != Account.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only STAFF or ADMIN can verify posts");
        }

        // 2) Load product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 3) Chỉ cho phép xử lý nếu post đang ở trạng thái PENDING_REVIEW
        if (product.getStatus() != Product.Status.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only posts in PENDING_REVIEW status can be verified or rejected");
        }

        // Xử lý action
        if (request.getAction() == PostVerifyRequest.ActionType.ACTIVE) {
            product.setStatus(Product.Status.ACTIVE);
            product.setRejectReason(null);

            // 4) LOGIC MỚI: Xử lý thông số kỹ thuật xe sau khi DUYỆT BÀI
            if (isVehicleProduct(product)) {
                generateAndSaveVehicleSpecs(product);
            }

        } else if (request.getAction() == PostVerifyRequest.ActionType.REJECT) {
            product.setStatus(Product.Status.REJECTED);
            product.setRejectReason(request.getRejectReason());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action");
        }

        product.setApprovedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        return PostVerifyMapper.mapToPostVerifyResponse(product);
    }

    private boolean isVehicleProduct(Product product) {
        return product.getType() != null && "VEHICLE".equals(product.getType().name());
    }

    // Generate và Lưu thông số kỹ thuật
    private void generateAndSaveVehicleSpecs(Product product) {

        ModelVersion version = product.getModelVersion();

        VehicleDetails details = vehicleDetailsRepository.findByProductId(product.getId()).orElse(null);

        // 1. Kiểm tra và lấy các Entity cơ bản
        if (version == null || version.getModel() == null) {
            log.warn("Product ID {} is missing ModelVersion or Model. Cannot generate specs.", product.getId());
            return;
        }

        Model model = version.getModel();

        // 2. Lấy 4 thuộc tính cần thiết qua chuỗi quan hệ
        // Brand và Type (VehicleType) nằm trong Model
        VehicleBrands brand = model.getBrand();
        VehicleCategories type = model.getVehicleType();

        // Tên Model và Title
        String productName = product.getTitle();
        String modelName = model.getName();
        String brandName = model.getBrand().getName();
        String versionName = version.getName();
        Short resolvedYear = product.getManufactureYear();

        // Kiểm tra tính toàn vẹn dữ liệu
        if (brand == null || type == null) {
            log.error("Model ID {} is missing Brand or VehicleType. Cannot generate specs.", model.getId());
            return;
        }

        if (resolvedYear == null) {
            log.warn("Product {} missing manufacture year. Defaulting to current year.", product.getId());
            resolvedYear = (short) LocalDateTime.now().getYear();
        }

        // 3. Kiểm tra VehicleCatalog đã có thông số cho ModelVersion này chưa
        Optional<VehicleCatalog> existingCatalog = vehicleCatalogRepository.findByVersionId(version.getId());

        if (existingCatalog.isEmpty()) {
            log.info("Vehicle spec not found for ModelVersion {}. Generating new specs using Gemini...",
                    version.getId());

            try {
                // 4. Gọi Gemini để generate specs DTO
                VehicleCatalogDTO specsDto = geminiRestService.getVehicleSpecs(
                        productName,
                        modelName,
                        brandName,
                        versionName, resolvedYear);

                // 5. Ánh xạ DTO sang Entity và lưu vào DB
                VehicleCatalog newCatalog = VehicleCatalogMapper.mapFromDto(specsDto);

                // Gán các foreign key & trường bắt buộc
                newCatalog.setVersion(version);
                newCatalog.setCategory(type); // Lấy từ Model
                newCatalog.setBrand(brand); // Lấy từ Model
                newCatalog.setModel(modelName);
                newCatalog.setYear(resolvedYear);

                vehicleCatalogRepository.save(newCatalog);
                log.info("Successfully generated and saved new VehicleCatalog for ModelVersion {}", version.getId());

            } catch (Exception e) {
                log.error("Failed to generate or save vehicle specs for Product ID {}: {}", product.getId(),
                        e.getMessage(), e);
            }
        } else {
            log.info("Vehicle spec already exists for ModelVersion {}. Skipping Gemini generation.", version.getId());
        }
    }

    @Transactional
    public List<PostVerifyResponse> getListVerifyPost() {

        List<Product> products = productRepository.findByStatus(Product.Status.PENDING_REVIEW);
        List<PostVerifyResponse> responses = new ArrayList<>();
        for (Product product : products) {
            PostPayment payment = postPaymentRepository
                    .findTopByProductIdAndPaymentStatusOrderByIdDesc(
                            product.getId(),
                            PostPayment.PaymentStatus.COMPLETED)
                    .orElse(null);
            PostVerifyResponse response = PostVerifyMapper.mapToPostVerifyResponse(product, payment);
            responses.add(response);
        }
        return responses;
    }

    @Transactional
    public List<PostVerifyResponse> getListVerifyPostByType(Product.ProductType type) {
        List<Product> products;
        if (type != null) {
            products = productRepository.findByStatusAndType(Product.Status.PENDING_REVIEW, type);
        } else {
            products = productRepository.findByStatus(Product.Status.PENDING_REVIEW);
        }
        List<PostVerifyResponse> responses = new ArrayList<>();
        for (Product product : products) {
            PostPayment payment = postPaymentRepository
                    .findTopByProductIdAndPaymentStatusOrderByIdDesc(
                            product.getId(),
                            PostPayment.PaymentStatus.COMPLETED)
                    .orElse(null);
            PostVerifyResponse response = PostVerifyMapper.mapToPostVerifyResponse(product, payment);
            responses.add(response);
        }
        return responses;
    }

}
