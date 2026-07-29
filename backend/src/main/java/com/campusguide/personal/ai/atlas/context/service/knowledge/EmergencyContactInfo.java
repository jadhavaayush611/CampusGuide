package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactInfo {
    private String contactId;
    private String serviceName;
    private String phoneNumber;
    private String altPhone;
    private String location;
    private boolean available24x7;
}
