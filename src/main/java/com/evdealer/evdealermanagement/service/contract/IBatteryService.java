package com.evdealer.evdealermanagement.service.contract;

import java.util.List;

public interface IBatteryService {
    List<Long> getBatteryIdByName(String name);
}
