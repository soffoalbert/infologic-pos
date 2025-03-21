package com.infologic.pos.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infologic.pos.dto.LoginRequest;
import com.infologic.pos.dto.MfaVerificationRequest;
import com.infologic.pos.dto.RegisterRequest;
import com.infologic.pos.dto.TokenResponse;
import com.infologic.pos.model.Role;
import com.infologic.pos.model.User;
import com.infologic.pos.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and User Registration API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully", 
                content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for username: {}", registerRequest.getUsername());
        
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty()) {
            registerRequest.getRoles().forEach(role -> {
                try {
                    roles.add(Role.valueOf(role));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role: {}", role);
                }
            });
        }
        
        // If no valid roles provided, assign default CASHIER role
        if (roles.isEmpty()) {
            roles.add(Role.ROLE_CASHIER);
        }
        
        User user = authService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPhoneNumber(),
                registerRequest.getTenantId(),
                roles
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticate a user and generate JWT token or prompt for MFA verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful",
                content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUsername());
        
        User authenticatedUser = authService.authenticateUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        
        // Check if MFA is enabled
        if (authenticatedUser.isMfaEnabled()) {
            // Return user ID for MFA verification step
            return ResponseEntity.ok(new TokenResponse(null, authenticatedUser.getId(), true));
        }
        
        // Generate JWT token
        String token = authService.generateToken(authenticatedUser);
        return ResponseEntity.ok(new TokenResponse(token, null, false));
    }
    
    @PostMapping("/mfa/enable/{userId}")
    @Operation(summary = "Enable MFA for user", description = "Enable Multi-Factor Authentication for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MFA setup initiated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> enableMfa(
            @Parameter(description = "ID of the user to enable MFA for") 
            @PathVariable Long userId) {
        log.info("MFA enable request received for user ID: {}", userId);
        
        String qrCodeUri = authService.enableMfa(userId);
        return ResponseEntity.ok(qrCodeUri);
    }
    
    @PostMapping("/mfa/verify")
    @Operation(summary = "Verify MFA code", description = "Verify the MFA code and generate a JWT token if verification is successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MFA verification successful", 
                content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid verification code"),
        @ApiResponse(responseCode = "401", description = "Verification failed")
    })
    public ResponseEntity<TokenResponse> verifyMfa(@Valid @RequestBody MfaVerificationRequest mfaRequest) {
        log.info("MFA verification request received for user ID: {}", mfaRequest.getUserId());
        
        String token = authService.verifyMfaAndGenerateToken(
                mfaRequest.getUserId(),
                mfaRequest.getCode()
        );
        
        return ResponseEntity.ok(new TokenResponse(token, null, false));
    }
} 