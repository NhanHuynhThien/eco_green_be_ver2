package com.evdealer.evdealermanagement.dto.post.verification;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVerifyRequest {

    @NotNull(message = "Action must not be null")
    private ActionType action;

    private String rejectReason;

    public enum ActionType {
        ACTIVE, REJECT
    }

}
