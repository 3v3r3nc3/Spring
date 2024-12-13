package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseBuildRequest {
    private Long productId;
    private Long ownerId;
    private Long licenseTypeId;
    private Long count;
}
