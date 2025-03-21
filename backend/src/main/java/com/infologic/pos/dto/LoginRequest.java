package com.infologic.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user authentication")
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "Username of the user", example = "johndoe", required = true)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Schema(description = "Password of the user", example = "P@ssw0rd123", required = true)
    private String password;
} 