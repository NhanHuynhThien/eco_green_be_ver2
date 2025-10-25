package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.product.ProductMapper;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    public ProductRepository productRepository;

    private final PostPaymentRepository postPaymentRepository;

    @Autowired
    public AccountRepository accountRepository;

    public List<ProductDetail> getAllProducts() {
        try {
            log.debug("Fetching all products");
            List<ProductDetail> list = productRepository.findAll()
                    .stream()
                    .map(ProductMapper::toDetailDto)
                    .toList();

            List<ProductDetail> sortedList = new ArrayList<>(list);
            sortedList.sort(Comparator.comparing(ProductDetail::getCreatedAt));

            return sortedList;
        } catch (Exception e) {
            log.error("Error fetching all products", e);
            return List.of();
        }
    }

    public List<Account> getMemberAccounts() {
        return getAccountsByRole(Account.Role.MEMBER);
    }

    public List<Account> getStaffAccounts() {
        return getAccountsByRole(Account.Role.STAFF);
    }

    public List<Account> getAccountsByRole(Account.Role role) {
        try {
            List<Account> accountList = accountRepository.findByRole(role)
                    .stream()
                    .sorted(Comparator.comparing(Account::getCreatedAt))
                    .toList();

            log.debug("Fetching all accounts with role: {}", role);
            return accountList;
        } catch (Exception e) {
            log.error("Error fetching accounts with role: {}", role, e);
            return List.of();
        }
    }

    public boolean deleteAccount(String id) {
        try {
            log.debug("Deleting account with id: {}", id);
            if (accountRepository.existsById(id)) {
                accountRepository.deleteById(id);
                return true;
            } else {
                log.warn("Account with id: {} not found", id);
                return false;
            }
        } catch (Exception e) {
            log.error("Error deleting account with id: {}", id, e);
            return false;
        }
    }

    public boolean changeStatusAccount(String id, Account.Status status) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account != null) {
            log.warn("Account with id: {}", id);
            account.setStatus(status);
            return true;
        }
        else {
            log.warn("Account with id: {} not found", id);
            return false;
        }
    }

    public String getTotalFee() {
        try {
            List<Product> productList = productRepository.findAll();

            BigDecimal totalFee = productList.stream()
                    .map(Product::getPostingFee)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.debug("Total import fee calculated: " + totalFee);
            return PriceSerializer.formatPrice(totalFee);

        } catch (Exception e) {
            log.error("Error calculating total import fee", e);
            return "0";
        }
    }

    public String getTotalFeeDuringMonth(String monthStr) {
        try {
            int month = Integer.parseInt(monthStr);

            if (month< 1 || month > 12) {
                log.warn("Invalid input {}", month);
                return "0";
            }

            int currentYear = VietNamDatetime.nowVietNam().getYear();

            List<PostPayment> paymentList = postPaymentRepository.findAll();

            BigDecimal totalFee = paymentList.stream()
                    .filter(p -> p.getCreatedAt() != null)
                    .filter(p -> {
                        var createdAt = p.getCreatedAt();
                        return  createdAt.getYear() == currentYear && createdAt.getMonthValue() == month;
                    })
                    .map(PostPayment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.debug("Total posting fee: {}",totalFee);
            return PriceSerializer.formatPrice(totalFee);
        } catch (NumberFormatException e) {
            log.error("Invalid format: {}", monthStr, e);
            return "0";
        } catch (Exception e) {
            log.error("Error calculating total fee for month: {}", monthStr, e);
            return "0";

        }
    }

}
