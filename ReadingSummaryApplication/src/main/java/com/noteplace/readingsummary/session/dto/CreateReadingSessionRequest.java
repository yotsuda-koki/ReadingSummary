package com.noteplace.readingsummary.session.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateReadingSessionRequest(
	    @NotNull LocalDate sessionDate,
	    @NotNull @Min(1) Integer minutes,
	    @Min(0) Integer pagesRead,
	    String memo
	) {}
