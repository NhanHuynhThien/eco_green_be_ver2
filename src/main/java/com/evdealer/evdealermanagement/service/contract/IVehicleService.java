package com.evdealer.evdealermanagement.service.contract;

import java.util.List;

public interface IVehicleService {
    List<Long> getVehicleIdByName(String name);
}
