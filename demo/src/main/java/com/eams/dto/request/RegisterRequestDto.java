package com.eams.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Self-service Sign Up payload.
 *
 * <p>Mirrors the four fields presented on the Sign Up tab:
 * firstName, lastName, email, password.
 *
 * <p>Password rules enforced server-side as a defence-in-depth check —
 * the same rules are also enforced live on the client:
 *   <ul>
 *     <li>at least 8 characters,</li>
 *     <li>at least one letter,</li>
 *     <li>at least one digit,</li>
 *     <li>at least one special character.</li>
 *   </ul>
 */
@Data
public class RegisterRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be 100 characters or fewer")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be 100 characters or fewer")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,128}$",
        message = "Password must contain at least one letter, one number and one special character"
    )
    private String password;
}
