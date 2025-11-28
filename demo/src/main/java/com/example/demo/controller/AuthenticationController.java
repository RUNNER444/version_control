package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ChangePasswordRequestDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.service.AuthenticationService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @CookieValue(name = "access-token", required = false) String access,
            @CookieValue(name = "refresh-token", required = false) String refresh,
            @RequestBody LoginRequestDto request) {
        logger.info("Received request to login user {}", request.username());
        
        try {
            return authenticationService.login(request, access, refresh);
        }
        catch (Exception e) {
            logger.error("Error while logging in user {}. Error: {}", request.username(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @CookieValue(name = "refresh-token", required = false) String refresh) {
        logger.info("Received request to refresh tokens");

        try {
            return authenticationService.refresh(refresh);
        }
        catch (Exception e) {
            logger.error("Error while refreshing tokens: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDto> logout(
            @CookieValue(name = "access-token", required = false) String access) {
        logger.info("Received request to logout user");

        try {
            return authenticationService.logout(access);
        }
        catch (Exception e) {
            logger.error("Error while logging out user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        
    }

    @GetMapping("/info")
    public ResponseEntity <UserLoggedDto> info() {
        logger.info("Received request to show info about user");

        try {
            return ResponseEntity.ok(authenticationService.info());
        }
        catch (Exception e) {
            logger.error("Error while showing info about user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PatchMapping("/changePassword")
    public ResponseEntity <LoginResponseDto> changePassword(ChangePasswordRequestDto request) {
        logger.info("Received request to change password");

        try {
            return authenticationService.changePassword(request);
        }
        catch (Exception e) {
            logger.error("Error while changing user's password: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}