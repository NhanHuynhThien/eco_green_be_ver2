package com.evdealer.evdealermanagement.controller.admin;

import com.evdealer.evdealermanagement.dto.account.response.ApiResponse;
import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class AdminProductManagementController {

    private final AdminService adminService;

    public AdminProductManagementController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<List<ProductDetail>> getAllProducts() {
        List<ProductDetail> products = adminService.getAllProducts();
        return new ApiResponse<>(200, "Fetched all products", products);
    }

    @GetMapping("/all-posting-fee")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<String> getAllPostingFee() {
       try {
           String totalPostingFee = adminService.getTotalFee();
           return new ApiResponse<>(200, "Total Fee", totalPostingFee);
       } catch (Exception e) {
           throw new AppException(ErrorCode.BAD_REQUEST);
       }
    }
}
