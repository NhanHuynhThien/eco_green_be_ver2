    package com.evdealer.evdealermanagement.controller.staff;

    import com.evdealer.evdealermanagement.dto.common.PageResponse;
    import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
    import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
    import com.evdealer.evdealermanagement.service.implement.ProductService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.data.web.PageableDefault;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.util.List;

    @RestController
    @RequestMapping("/staff/product")
    @Slf4j
    @RequiredArgsConstructor
    public class StaffProductManagementController {

        private final ProductService productService;

        @GetMapping("/by-status")
        @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
        public ResponseEntity<PageResponse<PostVerifyResponse>> getAllProductsWithStatus(@RequestParam String status
                , @PageableDefault(page = 0, size = 12, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
            try {
                log.info("Request → Get all products by status: {}", status);
                PageResponse<PostVerifyResponse> products = productService.getAllProductsWithStatus(status.toUpperCase(), pageable);

                if (products.getItems().isEmpty()) {
                    log.info("No  products found");
                    return ResponseEntity.noContent().build();
                }

                log.info("Found {} products", products.getItems().size());
                return ResponseEntity.ok(products);
            } catch (Exception e) {
                log.error("Error getting all products", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }


        @GetMapping("/status/all")
        @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
        public ResponseEntity<PageResponse<PostVerifyResponse>> getAllProductsWithStatusAll(@PageableDefault(page = 0, size = 12, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
            try {
                log.info("Request → Get all products");
                PageResponse<PostVerifyResponse> products = productService.getAllProductsWithStatusAll(pageable);

                if (products.getItems().isEmpty()) {
                    log.info("No  products found");
                    return ResponseEntity.noContent().build();
                }
                log.info("Found {} products", products.getItems().size());
                return ResponseEntity.ok(products);
            } catch (Exception e) {
                log.error("Error getting all products", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        }

    }
