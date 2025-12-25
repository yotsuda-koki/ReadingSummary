package com.noteplace.readingsummary.summary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSummaryRequest(
    @NotNull String scope,     // "CHAPTER" or "BOOK"
    Integer chapter,           // scope=CHAPTER のときのみ使う
    @NotBlank String contentMd
) {}
