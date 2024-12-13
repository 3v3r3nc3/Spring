package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBuildRequest {
    private String name;
    private Boolean isBlocked;
}
