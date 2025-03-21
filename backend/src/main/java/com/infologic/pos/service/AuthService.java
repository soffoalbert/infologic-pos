package com.infologic.pos.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infologic.pos.config.tenant.TenantContext;
import com.infologic.pos.exception.ResourceAlreadyExistsException;
import com.infologic.pos.exception.ResourceNotFoundException;
import com.infologic.pos.model.Role;
import com.infologic.pos.model.User;
import com.infologic.pos.repository.UserRepository;
import com.infologic.pos.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MfaService mfaService;

    /**
     * Register a new user
     * @param username username
     * @param password password
     * @param firstName first name
     * @param lastName last name
     * @param email email
     * @param phoneNumber phone number
     * @param tenantId tenant ID
     * @param roles roles
     * @return the created user
     */
    @Transactional
    public User registerUser(String username, String password, String firstName, String lastName, 
                             String email, String phoneNumber, String tenantId, Set<Role> roles) {
        log.info("Registering new user: {}", username);
        
        // Set tenant context for this operation
        TenantContext.setCurrentTenant(tenantId);
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username already exists: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already exists: " + email);
        }
        
        // Create new user
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phoneNumber)
                .tenantId(tenantId)
                .roles(roles != null ? roles : new HashSet<>())
                .build();
        
        // Generate MFA secret
        String mfaSecret = mfaService.generateSecret();
        user.setMfaSecret(mfaSecret);
        
        return userRepository.save(user);
    }
    
    /**
     * Enable MFA for a user
     * @param userId user ID
     * @return QR code image URI
     */
    @Transactional
    public String enableMfa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Set tenant context
        TenantContext.setCurrentTenant(user.getTenantId());
        
        // Enable MFA
        user.setMfaEnabled(true);
        userRepository.save(user);
        
        // Generate QR code
        return mfaService.generateQrCodeImageUri(user.getMfaSecret(), user.getUsername());
    }
    
    /**
     * Authenticate a user with username and password
     * @param username username
     * @param password password
     * @return authenticated user
     */
    public User authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (User) authentication.getPrincipal();
    }
    
    /**
     * Verify MFA code
     * @param userId user ID
     * @param code MFA code
     * @return JWT token if verification is successful
     */
    public String verifyMfaAndGenerateToken(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Set tenant context
        TenantContext.setCurrentTenant(user.getTenantId());
        
        // Verify code
        if (user.isMfaEnabled()) {
            boolean isCodeValid = mfaService.verifyCode(code, user.getMfaSecret());
            if (!isCodeValid) {
                throw new IllegalArgumentException("Invalid MFA code");
            }
        }
        
        // Generate token
        return jwtTokenProvider.generateToken(user);
    }
    
    /**
     * Generate token for a user (skipping MFA for users without MFA enabled)
     * @param user the authenticated user
     * @return JWT token or null if MFA is required
     */
    public String generateToken(User user) {
        // If MFA is enabled, we need to verify the code first
        if (user.isMfaEnabled()) {
            return null; // Client needs to provide MFA code
        }
        
        // Generate token
        return jwtTokenProvider.generateToken(user);
    }
} 