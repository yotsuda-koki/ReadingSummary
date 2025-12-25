package com.noteplace.readingsummary.session.dto;

import java.time.LocalDate;

public record ReadingSessionResponse(
	    Long id,
	    LocalDate sessionDate,
	    Integer minutes,
	    Integer pagesRead,
	    String memo
	) {}
