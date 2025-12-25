package com.noteplace.readingsummary.summary.dto;

import java.time.Instant;

public record SummaryResponse(
    Long id,
    String scope,
    Integer chapter,
    String contentMd,
    Instant createdAt,
    Instant updatedAt
) {}
