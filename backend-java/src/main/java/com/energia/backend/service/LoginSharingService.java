package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.LoginSharingConsentRequest;
import com.energia.backend.dto.LoginSharingPublicStatusDto;
import com.energia.backend.dto.LoginSharingRequestDto;
import com.energia.backend.dto.LoginSharingResponseDto;
import com.energia.backend.dto.LoginSharingUserDataDto;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.LoginSharingRequestEntity;
import com.energia.backend.model.LoginSharingStatus;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.LoginSharingRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginSharingService {

        private static final long PUBLIC_TOKEN_TTL_MINUTES = 30L;

        private final LoginSharingRepository loginSharingRepository;
        private final AppUserJpaRepository appUserRepository;
        private final UserStatusService userStatusService;

        public LoginSharingService(
                LoginSharingRepository loginSharingRepository,
                AppUserJpaRepository appUserRepository,
                UserStatusService userStatusService) {

                this.loginSharingRepository = loginSharingRepository;
                this.appUserRepository = appUserRepository;
                this.userStatusService = userStatusService;
        }

        /**
         * External agent requests user login data
         */
        @Transactional
        public LoginSharingResponseDto requestLoginData(LoginSharingRequestDto request) {

                log.info(
                        "External agent {} is requesting login data for user {}",
                        request.getExternalAgentName(),
                        request.getUserEmail());

                AppUserEntity user = appUserRepository
                        .findByEmailIgnoreCase(request.getUserEmail())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with email: " + request.getUserEmail()));

                LoginSharingRequestEntity sharingRequest = LoginSharingRequestEntity.builder()
                        .externalAgentName(request.getExternalAgentName())
                        .externalAgentEmail(request.getExternalAgentEmail())
                        .user(user)
                        .status(LoginSharingStatus.PENDING)
                        .publicToken(generatePublicToken())
                        .publicTokenExpiresAt(LocalDateTime.now().plusMinutes(PUBLIC_TOKEN_TTL_MINUTES))
                        .build();

                sharingRequest = loginSharingRepository.saveAndFlush(sharingRequest);

                log.info(
                        "Login sharing request created with id: {} for user: {}",
                        sharingRequest.getId(),
                        user.getId());

                log.info(
                        "Generated public token for request {} expiring {}",
                        sharingRequest.getId(),
                        sharingRequest.getPublicTokenExpiresAt());

                return LoginSharingResponseDto.fromEntity(sharingRequest);
        }   

        /**
         * Get pending sharing requests for a user
         */
        public List<LoginSharingResponseDto> getUserPendingRequests(UUID userId) {

                log.info("getUserPendingRequests called for userId={}", userId);

                List<LoginSharingRequestEntity> requests = loginSharingRepository
                        .findByUser_IdAndStatus(userId, LoginSharingStatus.PENDING);

                log.info("getUserPendingRequests result count={} for userId={}", requests.size(), userId);

                return requests.stream()
                        .map(LoginSharingResponseDto::fromEntity)
                        .toList();
        }

        /**
         * Get a specific sharing request for user consent
         */
        public LoginSharingResponseDto getRequestForConsent(UUID requestId, UUID userId) {

                LoginSharingRequestEntity request = loginSharingRepository
                        .findByIdAndUser_Id(requestId, userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Login sharing request not found or does not belong to this user"));

                if (!LoginSharingStatus.PENDING.equals(request.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Request is no longer pending. Current status: " + request.getStatus());
                }

                if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Request has expired");
                }

                return LoginSharingResponseDto.fromEntity(request);
        }

        /**
         * Read a sharing request directly by id, without requiring user context.
         */
        public LoginSharingResponseDto getRequestById(UUID requestId) {
                LoginSharingRequestEntity request = loginSharingRepository
                                .findById(requestId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Login sharing request not found"));

                return LoginSharingResponseDto.fromEntity(request);
        }

        public LoginSharingPublicStatusDto getPublicRequestStatus(UUID requestId, String token) {
                LoginSharingRequestEntity request = validatePublicToken(requestId, token);

                refreshExpiredRequestIfNeeded(request);

                return LoginSharingPublicStatusDto.fromEntity(request);
        }

        /**
         * User approves or rejects the login sharing request
         */
        @Transactional
        public LoginSharingResponseDto respondToRequest(
                UUID requestId,
                UUID userId,
                LoginSharingConsentRequest consent) {

                LoginSharingRequestEntity request = loginSharingRepository
                        .findByIdAndUser_Id(requestId, userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Login sharing request not found"));

                if (!LoginSharingStatus.PENDING.equals(request.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Request is no longer pending");
                }

                if (request.getExpiresAt().isBefore(LocalDateTime.now())) {

                request.setStatus(LoginSharingStatus.EXPIRED);
                loginSharingRepository.save(request);

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Request has expired");
                }

                request.setRespondedAt(LocalDateTime.now());
                request.setReason(consent.getReason());

                if (Boolean.TRUE.equals(consent.getApproved())) {

                request.setStatus(LoginSharingStatus.APPROVED);

                log.info(
                        "User {} approved login data sharing with agent {}",
                        userId,
                        request.getExternalAgentName());

                } else {

                request.setStatus(LoginSharingStatus.REJECTED);

                log.info(
                        "User {} rejected login data sharing with agent {}",
                        userId,
                        request.getExternalAgentName());
                }

                request = loginSharingRepository.save(request);

                return LoginSharingResponseDto.fromEntity(request);
            }

            public LoginSharingUserDataDto getPublicUserDataByRequest(UUID requestId, String token) {
                LoginSharingRequestEntity request = validatePublicToken(requestId, token);

                refreshExpiredRequestIfNeeded(request);

                if (!LoginSharingStatus.APPROVED.equals(request.getStatus())) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "User has not approved data sharing for this request");
                }

                AppUserEntity user = request.getUser();

                log.info(
                        "Public user data requested for request {}",
                        requestId);

                return LoginSharingUserDataDto.builder()
                        .email(user.getEmail())
                        .nomeCompleto(user.getName())
                        .telefone(user.getPhone())
                        .status(userStatusService.resolveCurrentStatus(user).toString())
                        .message("User data retrieved successfully")
                        .build();
    }

        /**
         * External agent retrieves user data if approved
         */
        public LoginSharingUserDataDto getUserDataByRequest(UUID requestId) {

                LoginSharingRequestEntity request = loginSharingRepository
                        .findById(requestId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Login sharing request not found"));

                if (!LoginSharingStatus.APPROVED.equals(request.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User has not approved data sharing for this request");
                }

                if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Request has expired");
                }

                AppUserEntity user = request.getUser();

                log.info(
                        "External agent {} is retrieving user data via approved request {}",
                        request.getExternalAgentName(),
                        requestId);

                return LoginSharingUserDataDto.builder()
                        .email(user.getEmail())
                        .nomeCompleto(user.getName())
                        .telefone(user.getPhone())
                        .status(userStatusService.resolveCurrentStatus(user).toString())
                        .message("User data retrieved successfully")
                        .build();
        }

        private LoginSharingRequestEntity validatePublicToken(UUID requestId, String token) {
                LoginSharingRequestEntity request = loginSharingRepository
                                .findById(requestId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Login sharing request not found"));

                LocalDateTime now = LocalDateTime.now();
                boolean tokenMissingOrInvalid = token == null || token.isBlank()
                                || request.getPublicToken() == null
                                || !request.getPublicToken().equals(token)
                                || request.getPublicTokenExpiresAt() == null
                                || !request.getPublicTokenExpiresAt().isAfter(now);

                if (tokenMissingOrInvalid) {
                        log.warn("Invalid/expired public token for request {}", requestId);
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Invalid or expired public token");
                }

                return request;
        }

        private void refreshExpiredRequestIfNeeded(LoginSharingRequestEntity request) {
                if (LoginSharingStatus.PENDING.equals(request.getStatus())
                                && request.getExpiresAt() != null
                                && !request.getExpiresAt().isAfter(LocalDateTime.now())) {
                        request.setStatus(LoginSharingStatus.EXPIRED);
                        loginSharingRepository.save(request);
                }
        }

        private void invalidatePublicToken(LoginSharingRequestEntity request) {
                request.setPublicToken(null);
                request.setPublicTokenExpiresAt(null);
                loginSharingRepository.save(request);
        }

        private String generatePublicToken() {
                return UUID.randomUUID().toString();
        }

        /**
         * Get all sharing requests for a user (for user to see history)
         */
        public List<LoginSharingResponseDto> getUserAllRequests(UUID userId) {

                log.info("getUserAllRequests called for userId={}", userId);

                List<LoginSharingRequestEntity> requests =
                        loginSharingRepository.findByUser_Id(userId);

                log.info("getUserAllRequests result count={} for userId={}", requests.size(), userId);

                return requests.stream()
                        .map(LoginSharingResponseDto::fromEntity)
                        .toList();
        }

        /**
         * Clean up expired requests
         */
                @Scheduled(fixedDelayString = "${login-sharing.cleanup.delay-ms:60000}")
        @Transactional
        public void cleanupExpiredRequests() {

                LocalDateTime now = LocalDateTime.now();

                List<LoginSharingRequestEntity> expiredRequests =
                        loginSharingRepository.findByStatusAndExpiresAtBefore(
                                LoginSharingStatus.PENDING,
                                now);

                expiredRequests.forEach(request -> {
                request.setStatus(LoginSharingStatus.EXPIRED);
                loginSharingRepository.save(request);
                });

                if (!expiredRequests.isEmpty()) {
                log.info(
                        "Cleaned up {} expired login sharing requests",
                        expiredRequests.size());
                }
        }

        @Transactional
        public LoginSharingResponseDto revokeRequest(UUID requestId, UUID userId) {
        LoginSharingRequestEntity request = loginSharingRepository
                .findByIdAndUser_Id(requestId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Login sharing request not found"));

        if (!LoginSharingStatus.APPROVED.equals(request.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only approved requests can be revoked");
        }

        request.setStatus(LoginSharingStatus.REVOKED);
        request.setRespondedAt(LocalDateTime.now());
        request = loginSharingRepository.save(request);

        log.info("User {} revoked login data sharing with agent {} for request {}",
                userId, request.getExternalAgentName(), requestId);

        return LoginSharingResponseDto.fromEntity(request);
        }
}