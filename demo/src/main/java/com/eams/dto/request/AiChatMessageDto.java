package com.eams.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class AiChatMessageDto {
    @NotBlank
    private String message;
    private UUID sessionId;
}
