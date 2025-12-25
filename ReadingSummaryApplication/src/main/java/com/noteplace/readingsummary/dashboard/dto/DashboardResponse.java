package com.noteplace.readingsummary.dashboard.dto;

public record DashboardResponse(
	    int streakDays,
	    long thisMonthMinutes,
	    int thisMonthReadingDays,
	    long totalBooks,
	    long unreadBooks,
	    long readingBooks,
	    long doneBooks,
	    long totalSummaries
	) {}
