package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ChangePasswordRequestDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Token;
import com.example.demo.model.User;
import com.example.demo.repository.TokenRepository;
import com.example.demo.util.CookieUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Value("${jwt.access.duration.minute}")
    private long accessDurationMin;
    @Value("${jwt.access.duration.second}")
    private long accessDurationSec;
    @Value("${jwt.refresh.duration.days}")
    private long refreshDurationDate;
    @Value("${jwt.refresh.duration.second}")
    private long refreshDurationSec;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        logger.debug("Adding access token cookie");
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
        logger.debug("Adding refresh token cookie");
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) {
        Set <Token> tokens = user.getTokens();
        
        tokens.forEach(token -> {
            if(token.getExpiringDate().isBefore(LocalDateTime.now())) tokenRepository.delete(token);
            else if(!token.isDisabled()) {
                token.setDisabled(true);
                tokenRepository.save(token);
            }
        });
        logger.debug("Token revocation process completed for user: {}", user.getUsername());
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto request, String access, String refresh) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            request.username(), request.password()));
        User user = userService.getUser(request.username());
        logger.debug("User authenticated successfully: {}", user.getUsername());

        boolean accessValid = jwtTokenProvider.isValid(access);
        boolean refreshValid = jwtTokenProvider.isValid(refresh);
        logger.debug("Results of token validation for user {} - Access: {}, Refresh: {}", user.getUsername(), accessValid, refreshValid);

        HttpHeaders headers = new HttpHeaders();

        revokeAllTokens(user);

        if (!accessValid) {
            Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
            accessDurationMin, ChronoUnit.MINUTES, user);
            newAccess.setUser(user);
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);
        }

        if (!refreshValid || accessValid) {
            Token newRefresh = jwtTokenProvider.generatedRefreshToken(refreshDurationDate, ChronoUnit.MINUTES, user);
            newRefresh.setUser(user);
            addRefreshTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("Login completed successfully for user: {}", user.getUsername());
        
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity <LoginResponseDto> refresh(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken)) {
            logger.warn("Token refresh failed: Invalid token provided");
            throw new RuntimeException("Invalid token provided");
        }
        
        User user = userService.getUser(jwtTokenProvider.getUsername(refreshToken));
        Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
            accessDurationMin, ChronoUnit.MINUTES, user);
        
        newAccess.setUser(user);
        HttpHeaders headers = new HttpHeaders();
        addAccessTokenCookie(headers, newAccess);
        tokenRepository.save(newAccess);
        logger.info("Token refresh successful for user: {}", user.getUsername());

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(true, user.getRole().getName()));
    }

    public ResponseEntity <LoginResponseDto> logout(String accessToken) {
        SecurityContextHolder.clearContext();
        User user = userService.getUser(jwtTokenProvider.getUsername(accessToken));
        revokeAllTokens(user);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());
        logger.info("Logout successful for user: {}", user.getUsername());

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }

    public UserLoggedDto info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken){
            logger.warn("Info requested by unauthenticated user");
            throw new RuntimeException("User is not authenticated");
        }

        logger.debug("Retrieving info for authenticated user: {}", authentication.getName());
        User user = userService.getUser(authentication.getName());

        return UserMapper.userToUserLoggedDto(user);
    }

    public ResponseEntity <LoginResponseDto> changePassword(ChangePasswordRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken){
            logger.warn("Password change attempted by unauthenticated user");
            throw new RuntimeException("User is not authenticated");
        }

        User user = userService.getUser(authentication.getName());
        if(!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            logger.warn("Password change failed for user {}: Invalid old password", user.getUsername());
            throw new BadCredentialsException("Old password is invalid");
        }
        if (!request.newPassword().matches(request.newAgain())) {
            logger.warn("Password change failed for user {}: New passwords do not match", user.getUsername());
            throw new BadCredentialsException("New passwords don't match each other");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.saveUser(user);
        revokeAllTokens(user);
        SecurityContextHolder.clearContext();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());
        logger.info("Password changed successfully for user: {}. User logged out.", user.getUsername());

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDto(false, null));
    }
}
