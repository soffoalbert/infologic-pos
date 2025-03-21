package com.infologic.pos.service;

import org.springframework.stereotype.Service;
import java.util.Base64;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MfaService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    /**
     * Generate a new secret key for TOTP
     * @return the generated secret
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Generate a QR code for the TOTP setup
     * @param secret the secret key
     * @param username the username
     * @return Base64 encoded QR code image
     */
    public String generateQrCodeImageUri(String secret, String username) {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer("InfoLogic POS")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            // Convert byte[] to base64 encoded string
            byte[] qrCodeBytes = qrGenerator.generate(data);
            String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + base64QrCode;
        } catch (QrGenerationException e) {
            log.error("Error generating QR code", e);
            return null;
        }
    }

    /**
     * Verify a TOTP code
     * @param code the code to verify
     * @param secret the secret key
     * @return true if the code is valid
     */
    public boolean verifyCode(String code, String secret) {
        return codeVerifier.isValidCode(secret, code);
    }
} 