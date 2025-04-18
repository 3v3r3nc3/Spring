package ru.mtuci.everence.model;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureGUIDRequest {
    private List<UUID> guids;
}