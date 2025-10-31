package com.evdealer.evdealermanagement.mapper.notify;


import com.evdealer.evdealermanagement.dto.notify.NotificationResponse;
import com.evdealer.evdealermanagement.entity.notify.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring") //tự động tạo Spring Bean
public interface NotificationMapper {

    @Mapping(target = "isRead", source = "read")
    @Mapping(target = "type", source = "type", qualifiedByName = "enumToString")
    NotificationResponse toDTO(Notification notification);

    List<NotificationResponse> toDTO(List<Notification> notifications);

    @Named("enumToString")
    default String enumToString(Notification.NotificationType type) {
        return type != null ? type.toString() : null;
    }

}
