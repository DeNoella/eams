package com.eams.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AiChatResponseDto {
    private String message;
    private UUID sessionId;
}
