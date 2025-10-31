package com.evdealer.evdealermanagement.dto.notify;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {

    String id;
    String title;
    String content;
    String type;
    String refId;
    boolean isRead;
    LocalDateTime createdAt;
}
