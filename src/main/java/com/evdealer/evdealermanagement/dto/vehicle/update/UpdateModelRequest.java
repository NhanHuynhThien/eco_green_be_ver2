package com.evdealer.evdealermanagement.dto.vehicle.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateModelRequest {
    @NotBlank(message = "modelName is required")
    private String modelName;

    // Cho phép đổi category; nếu không đổi thì gửi lại id hiện tại
    @NotBlank(message = "vehicleCategoryId is required")
    private String vehicleCategoryId;
}
