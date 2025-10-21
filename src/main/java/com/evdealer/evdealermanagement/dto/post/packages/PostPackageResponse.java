package com.evdealer.evdealermanagement.dto.post.packages;

import com.evdealer.evdealermanagement.entity.post.PostPackage;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostPackageResponse {

    String postPackageId;
    String postPackageCode;
    String postPackageName;
    String postPackageDesc;
    PostPackage.BillingMode billingMode;
    PostPackage.Category category;
    Integer baseDurationDays;
    BigDecimal price;
    BigDecimal dailyPrice;
    Boolean includesPostFee;
    Integer priorityLevel;
    String badgeLabel;
    Boolean showInLatest;
    Boolean showTopSearch;
    BigDecimal listPrice;
    Boolean isDefault;
    String note; // “Miễn phí lần đầu tiên” cho Tin thường
    List<PostPackageOptionResponse> options;

}
