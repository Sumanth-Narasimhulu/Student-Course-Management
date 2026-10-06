package com.studentmanagement.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.studentmanagement.entity.RefreshToken;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.AuthenticationException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {
    private RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_DAYS = 7;
    private static final long MAX_SESSION_DAYS = 30;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String createRefreshToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        String newToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(newToken);
        refreshToken.setSessionExpiresAt(now.plusDays(MAX_SESSION_DAYS));
        refreshToken.setExpiresAt(now.plusDays(REFRESH_TOKEN_DAYS));
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
        return newToken;

    }

    @Transactional
    public RefreshToken rotateRefreshToken(String refreshToken) {
        RefreshToken oldRefreshToken = validateRefreshToken(refreshToken);
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        RefreshToken newRefreshToken = new RefreshToken();
        String newToken = UUID.randomUUID().toString();
        newRefreshToken.setRefreshToken(newToken);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpiry = now.plusDays(REFRESH_TOKEN_DAYS);
        if (newExpiry.isAfter(oldRefreshToken.getSessionExpiresAt())) {
            newExpiry = oldRefreshToken.getSessionExpiresAt();
        }
        newRefreshToken.setExpiresAt(newExpiry);
        newRefreshToken.setSessionExpiresAt(oldRefreshToken.getSessionExpiresAt());
        newRefreshToken.setUser(oldRefreshToken.getUser());
        return refreshTokenRepository.save(newRefreshToken);

    }

    @Transactional
    public RefreshToken validateRefreshToken(String refreshToken) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("refresh token not found"));
        LocalDateTime now = LocalDateTime.now();
        if (oldRefreshToken.getRevoked()) {
            throw new AuthenticationException("refresh token revoked");
        }

        LocalDateTime oldRefreshTokenexpiresAt = oldRefreshToken.getExpiresAt();
        if (oldRefreshTokenexpiresAt.isBefore(now)) {
            throw new AuthenticationException("refresh token expired");
        }
        LocalDateTime oldRefreshTokenSessionExpiresAt = oldRefreshToken.getSessionExpiresAt();
        if (oldRefreshTokenSessionExpiresAt.isBefore(now)) {
            throw new AuthenticationException("refresh token session expired");

        }
        return oldRefreshToken;

    }

    @Transactional
    public String revokeRefreshToken(String refreshToken) {
        RefreshToken currentRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("404 refresh token"));
        currentRefreshToken.setRevoked(true);
        refreshTokenRepository.save(currentRefreshToken);
        return "Successfully revoked";
    }
}
