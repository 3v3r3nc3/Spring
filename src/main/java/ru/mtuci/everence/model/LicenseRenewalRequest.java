package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseRenewalRequest {
    private String activateCode;
}
