package com.eams.service;

import com.eams.dto.request.AiChatMessageDto;
import com.eams.dto.response.AiChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI chat entrypoint: answers are produced only from tenant data via {@link AiDataAssistantService}
 * (no external paid LLM APIs).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiDataAssistantService aiDataAssistantService;

    public AiChatResponseDto chat(AiChatMessageDto dto) {
        String reply = aiDataAssistantService.answer(dto.getMessage());
        log.debug("Data-assistant reply generated for session {}", dto.getSessionId());
        return AiChatResponseDto.builder()
            .message(reply)
            .sessionId(dto.getSessionId())
            .build();
    }
}
