package com.evdealer.evdealermanagement.dto.battery.brand;

import com.fasterxml.jackson.annotation.JsonInclude;
<<<<<<< HEAD

=======
>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatteryBrandsResponse {

    String brandId;
    String brandName;
<<<<<<< HEAD

=======
>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
    String logoUrl;
}
