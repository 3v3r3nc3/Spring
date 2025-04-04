package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseTypeBuildRequest {
    private String name;
    private Long duration;
    private String description;

}
