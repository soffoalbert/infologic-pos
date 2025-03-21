package com.infologic.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing authentication token information")
public class TokenResponse {
    
    @Schema(description = "JWT token for authentication (null if MFA is required)", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "User ID (only returned when MFA verification is required)", example = "42")
    private Long userId;
    
    @Schema(description = "Flag indicating whether MFA verification is required", example = "true")
    private boolean mfaRequired;
} 