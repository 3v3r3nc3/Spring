package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEditRequest {
    private Long productId;
    private String name;
    private Boolean isBlocked;
}
