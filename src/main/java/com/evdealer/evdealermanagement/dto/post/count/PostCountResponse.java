package com.evdealer.evdealermanagement.dto.post.count;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCountResponse {
    private String memberId;
    private long totalPosts;
}
