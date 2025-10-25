package com.evdealer.evdealermanagement.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class VietNamDatetime {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private LocalDateTime nowVietNam () {
        return ZonedDateTime.now(VIETNAM_ZONE).toLocalDateTime();
    }
}
