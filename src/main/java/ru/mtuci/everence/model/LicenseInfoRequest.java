package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseInfoRequest {
    private String name;
    private String macAddress;
    private String activateCode;
}
