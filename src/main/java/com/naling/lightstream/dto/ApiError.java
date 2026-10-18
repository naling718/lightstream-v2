package com.naling.lightstream.dto;

public record ApiError(int status, String error, String message) {}
