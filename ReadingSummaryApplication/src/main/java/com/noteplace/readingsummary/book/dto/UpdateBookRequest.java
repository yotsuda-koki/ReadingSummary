package com.noteplace.readingsummary.book.dto;

import jakarta.validation.constraints.Min;

public record UpdateBookRequest(
	    String status,
	    @Min(0) Integer currentPage,
	    @Min(0) Integer totalPages
	) {}
