package com.infologic.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for MFA verification")
public class MfaVerificationRequest {
    
    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user to verify", example = "42", required = true)
    private Long userId;
    
    @NotBlank(message = "Verification code is required")
    @Schema(description = "The time-based OTP verification code", example = "123456", required = true)
    private String code;
} 