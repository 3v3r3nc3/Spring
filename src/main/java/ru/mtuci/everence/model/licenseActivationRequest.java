package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class licenseActivationRequest {
    private String activationCode;
    private String name;
    private String macAddress;
    private Long deviceId;
}
