package com.infologic.pos.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user registration")
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username for login", example = "johndoe", required = true, minLength = 3, maxLength = 50)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "Password for login", example = "P@ssw0rd123", required = true, minLength = 6, maxLength = 100)
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    @Schema(description = "First name of the user", example = "John", required = true, maxLength = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Schema(description = "Last name of the user", example = "Doe", required = true, maxLength = 50)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true, maxLength = 100)
    private String email;
    
    @Schema(description = "Phone number of the user", example = "+1234567890")
    private String phoneNumber;
    
    @NotBlank(message = "Tenant ID is required")
    @Schema(description = "ID of the tenant (organization) the user belongs to", example = "tenant1", required = true)
    private String tenantId;
    
    @Schema(description = "Roles assigned to the user", example = "[\"ROLE_ADMIN\", \"ROLE_VENDOR\"]")
    private Set<String> roles;
} 