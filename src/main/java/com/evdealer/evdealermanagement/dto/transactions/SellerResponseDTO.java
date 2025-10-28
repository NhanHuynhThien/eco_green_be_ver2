package com.evdealer.evdealermanagement.dto.transactions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDTO {

    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotNull(message = "Accept/Reject decision is required")
    private Boolean accept;

    @Size(max = 500, message = "Response message must not exceed 500 characters")
    private String responseMessage;

    @Size(max = 255, message = "Reject reason must not exceed 255 characters")
    private String rejectReason;
}
