package com.evdealer.evdealermanagement.controller.member;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.service.implement.RecentViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/recent")
@RequiredArgsConstructor
public class RecentViewController {

    private final RecentViewService recentViewService;

    @PostMapping("/{productId}")
    public void addRecent(@PathVariable String productId) {
        recentViewService.addRecentView(productId);
    }

    @GetMapping
    public List<ProductDetail> getUserRecentView() {
        return recentViewService.getRecentViewedProducts();
    }

}
