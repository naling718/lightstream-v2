package com.naling.lightstream.controller;

import com.naling.lightstream.dto.RegisterRequest;
import com.naling.lightstream.dto.UserResponse;
import com.naling.lightstream.service.AuthService;
import com.naling.lightstream.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.email(), request.password());
        return new UserResponse(user.getId(), user.getEmail());
    }
}

