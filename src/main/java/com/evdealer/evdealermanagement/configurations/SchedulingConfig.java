package com.evdealer.evdealermanagement.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig { // class naày giúp springboot biết bằng nào có @Scheduled sẽ đc gọi chạy tự động theo thời gian đã định.
}
