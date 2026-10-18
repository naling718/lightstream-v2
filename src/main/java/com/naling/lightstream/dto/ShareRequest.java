package com.naling.lightstream.dto;

import jakarta.validation.constraints.NotBlank;

public record ShareRequest (@NotBlank(message = "Text must not be blank") String text) {}
