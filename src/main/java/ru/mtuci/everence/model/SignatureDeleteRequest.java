package ru.mtuci.everence.model;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureDeleteRequest {
    private UUID signatureUUID;
}
