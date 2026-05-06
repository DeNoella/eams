package com.eams.dto.response;

import lombok.*;

@Getter @Setter @AllArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private String tokenType;
}