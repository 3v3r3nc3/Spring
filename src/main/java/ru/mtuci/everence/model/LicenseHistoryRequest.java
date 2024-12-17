package ru.mtuci.everence.model;

import lombok.*;



@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseHistoryRequest {
    private String first;
    private String last;
}
