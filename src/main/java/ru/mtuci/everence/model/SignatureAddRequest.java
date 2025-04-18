package ru.mtuci.everence.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureAddRequest {
    private String threatName;
    private String firstBytes;
    private Integer remainderLength;
    private String fileType;
    private Integer offsetStart;
    private Integer offsetEnd;
}
