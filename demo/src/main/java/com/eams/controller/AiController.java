package com.eams.controller;

import com.eams.dto.request.AiChatMessageDto;
import com.eams.dto.response.AiChatResponseDto;
import com.eams.dto.response.ApiResponse;
import com.eams.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AiChatResponseDto>> chat(@Valid @RequestBody AiChatMessageDto dto) {
        return ResponseEntity.ok(ApiResponse.success(aiService.chat(dto)));
    }
}
