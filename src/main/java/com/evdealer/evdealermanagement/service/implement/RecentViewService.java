package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.account.RecentView;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.RecentViewRepository;
import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecentViewService {

    private final UserContextService userContextService;
    private final ProductRepository productRepository;
    private final RecentViewRepository recentViewRepository;

    @Transactional
    public void addRecentView(String productId) {
        Account user = userContextService.getCurrentUser().orElse(null);

        if (user == null) {
            log.debug("Anonymous user view - skipping DB record for product {}", productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // xóa record cũ nếu bị trùng
        recentViewRepository.deleteByUserIdAndProductId(user.getId(), productId);

        // Lưu record mới
        RecentView view = new RecentView();
        view.setUser(user);
        view.setProduct(product);
        view.setViewedAt(VietNamDatetime.nowVietNam());

        recentViewRepository.save(view);

        // Chỉ giữ 10 record gần nhất
        List<RecentView> recentViews = recentViewRepository.findByUserIdOrderByViewedAtDesc(user.getId());
        if (recentViews.size() > 10) {
            recentViewRepository.deleteAll(recentViews.subList(10, recentViews.size()));
        }
        log.info("User {} viewed product {}", user.getEmail(), product.getId());
    }

    @Transactional(readOnly = true)
    public List<ProductDetail> getRecentViewedProducts() {
        Account user = userContextService.getCurrentUser().orElse(null);
        return recentViewRepository.findProductsByUserId(user.getId())
                .stream()
                .distinct() // Remove duplicates caused by images join
                .map(ProductDetail::fromEntity)
                .toList();
    }

}
