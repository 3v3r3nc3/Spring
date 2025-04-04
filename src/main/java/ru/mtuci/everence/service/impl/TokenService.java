package ru.mtuci.everence.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mtuci.everence.configuration.JwtTokenProvider;
import ru.mtuci.everence.model.ApplicationUser;
import ru.mtuci.everence.model.SessionStatus;
import ru.mtuci.everence.model.TokenResponse;
import ru.mtuci.everence.model.UserSession;
import ru.mtuci.everence.repository.UserRepository;
import ru.mtuci.everence.repository.UserSessionRepository;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import java.util.Set;

@Service
public class TokenService {

    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public TokenService(UserSessionRepository userSessionRepository, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.userSessionRepository = userSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public TokenResponse issueTokenPair(String email, Long deviceId, Set<GrantedAuthority> authorities) {
        String accessToken = jwtTokenProvider.createAccessToken(email, authorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, deviceId);

        UserSession newSession = createSession(email, deviceId, accessToken, refreshToken);
        userSessionRepository.save(newSession);

        return new TokenResponse(accessToken, refreshToken);
    }

    private UserSession createSession(String email, Long deviceId, String accessToken, String refreshToken) {
        Long currentTimeMillis = System.currentTimeMillis();

        UserSession session = new UserSession();
        session.setEmail(email);
        session.setDeviceId(deviceId);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setAccessTokenExpiry(new Date(currentTimeMillis + 5 * 60 * 1000)); // 5 минут
        session.setRefreshTokenExpiry(new Date(currentTimeMillis + 24 * 60 * 60 * 1000)); // 24 часа
        session.setStatus(SessionStatus.ACTIVE);

        return session;
    }

    public TokenResponse refreshTokenPair(Long deviceId, String refreshToken) {
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken).orElse(null);

        if (isInvalidSession(session, deviceId)) {
            if (session != null) {
                blockAllSessionsForUser(session.getEmail());
            }
            return null;
        }

        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        ApplicationUser user = userRepository.findByEmail(session.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return issueTokenPair(session.getEmail(), deviceId, user.getRole().getGrantedAuthorities());
    }

    private boolean isInvalidSession(UserSession session, Long deviceId) {
        return session == null || session.getStatus() != SessionStatus.ACTIVE || !Objects.equals(session.getDeviceId(), deviceId);
    }

    public void blockAllSessionsForUser(String email) {
        List<UserSession> sessions = userSessionRepository.findAllByEmail(email);

        for (UserSession session : sessions) {
            if (session.getStatus() == SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.REVOKED);
                userSessionRepository.save(session);
            }
        }
    }
}
