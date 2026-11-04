package com.naling.lightstream.controller;

import com.naling.lightstream.dto.LoginRequest;
import com.naling.lightstream.dto.LoginResponse;
import com.naling.lightstream.dto.RegisterRequest;
import com.naling.lightstream.dto.UserResponse;
import com.naling.lightstream.service.AuthService;
import com.naling.lightstream.service.TokenService;
import com.naling.lightstream.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.email(), request.password());
        return new UserResponse(user.getId(), user.getEmail());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String token = tokenService.issue(user);
        return new LoginResponse(token, "Bearer", tokenService.getTtl().toSeconds());
    }
}

