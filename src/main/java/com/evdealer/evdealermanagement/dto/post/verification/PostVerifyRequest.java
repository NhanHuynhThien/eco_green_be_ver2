package com.evdealer.evdealermanagement.dto.post.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVerifyRequest {

    @NotBlank(message = "Reject reason must not be empty")
    private String rejectReason;

}
