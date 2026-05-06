package com.eams.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MagicLinkRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;
}