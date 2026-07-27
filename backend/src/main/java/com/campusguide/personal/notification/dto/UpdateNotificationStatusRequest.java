package com.campusguide.personal.notification.dto;

import com.campusguide.personal.notification.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNotificationStatusRequest {

    @NotNull(message = "Status is mandatory")
    private NotificationStatus status;
}
