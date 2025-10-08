package com.evdealer.evdealermanagement.dto.post.battery;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import com.evdealer.evdealermanagement.entity.battery.BatteryBrands;
import com.evdealer.evdealermanagement.entity.battery.BatteryTypes;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class BatteryPostRequest {
    private String productId; // Mã sản phẩm
    private Double  capacityKwh; // Dung lượng pin (kWh)
    private int healthPercentage;
    private String origin;
    private int VoltageV;
    private BatteryBrands batteryBrands;
    private BatteryTypes batteryTypes;
    private String title;
    private String description;
    private Double price;
    private String location;
}
