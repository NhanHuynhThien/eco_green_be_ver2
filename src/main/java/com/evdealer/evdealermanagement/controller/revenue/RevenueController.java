package com.evdealer.evdealermanagement.controller.revenue;

import com.evdealer.evdealermanagement.dto.revenue.MonthlyRevenue;
import com.evdealer.evdealermanagement.service.implement.RevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
@Slf4j
public class RevenueController {

    private final RevenueService revenueService;

    /**
     * Lấy tổng doanh thu theo tháng (gộp theo năm)
     * @param month số tháng (1–12)
     * @return Danh sách doanh thu của từng năm trong tháng đó
     */
    @GetMapping("/month")
    public List<MonthlyRevenue> getRevenueByMonth(@RequestParam("month") String month) {
        log.info("Fetching revenue for month {}", month);
        return revenueService.getTotalFeeDuringMonth(month);
    }

}
