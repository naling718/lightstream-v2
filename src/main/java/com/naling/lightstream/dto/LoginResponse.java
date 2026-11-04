package com.naling.lightstream.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {}

