package com.noteplace.readingsummary.book.dto;


public record BookResponse(
  Long id, String title, String author, String language, String level,
  String status, Integer totalPages, Integer currentPage
) {}