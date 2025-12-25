package com.noteplace.readingsummary.common.error;

public record ApiError(String code, String message, String traceId) {}

