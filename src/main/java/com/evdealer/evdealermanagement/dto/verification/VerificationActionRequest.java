package com.evdealer.evdealermanagement.dto.verification;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationActionRequest {

    @NotNull(message = "Action must not be null")
    private ActionType action;

    private String rejectReason;

    public enum ActionType {
        ACTIVE, REJECT
    }

}
