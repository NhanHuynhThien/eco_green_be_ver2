package com.evdealer.evdealermanagement.service.contract;

import com.evdealer.evdealermanagement.dto.post.battery.BatteryPostRequest;
import com.evdealer.evdealermanagement.dto.post.battery.BatteryPostResponse;
import com.evdealer.evdealermanagement.dto.post.vehicle.VehiclePostRequest;
import com.evdealer.evdealermanagement.dto.post.vehicle.VehiclePostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductPostService {
    BatteryPostResponse createBatteryPost(String sellerId, BatteryPostRequest request, List<MultipartFile> images, String imagesMetaJson );

    VehiclePostResponse createVehiclePost(String sellerId, VehiclePostRequest request, List<MultipartFile> images, String imagesMetaJson );
}
