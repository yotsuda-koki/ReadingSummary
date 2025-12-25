package com.noteplace.readingsummary.book.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
		  @NotBlank String title,
		  String author,
		  String language,
		  String level,
		  @NotBlank String status,
		  Integer totalPages
		) {}
