package com.noteplace.readingsummary.dashboard.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.noteplace.readingsummary.book.repository.BookRepository;
import com.noteplace.readingsummary.dashboard.dto.DashboardResponse;
import com.noteplace.readingsummary.session.repository.ReadingSessionRepository;
import com.noteplace.readingsummary.summary.repository.SummaryRepository;

@Service
public class DashboardService {

  private final BookRepository bookRepo;
  private final ReadingSessionRepository sessionRepo;
  private final SummaryRepository summaryRepo;

  // ユーザーが日本（Asia/Tokyo）前提なので、streak判定は東京日付に寄せます
  private final ZoneId zone = ZoneId.of("Asia/Tokyo");


  public DashboardService(BookRepository bookRepo, ReadingSessionRepository sessionRepo, SummaryRepository summaryRepo) {
    this.bookRepo = bookRepo;
    this.sessionRepo = sessionRepo;
    this.summaryRepo = summaryRepo;
  }

  public DashboardResponse getDashboard(Long userId) {
    LocalDate today = LocalDate.now(zone);

    // 今月の範囲 [start, end)
    YearMonth ym = YearMonth.from(today);
    LocalDate startOfMonth = ym.atDay(1);
    LocalDate startOfNextMonth = ym.plusMonths(1).atDay(1);

    long thisMonthMinutes = sessionRepo.sumMinutesByUserIdBetween(userId, startOfMonth, startOfNextMonth);

    // streak計算用に、直近1年分の「読んだ日」を取る
    LocalDate startForStreak = today.minusDays(365);
    List<LocalDate> days = sessionRepo.findDistinctSessionDatesByUserIdBetween(userId, startForStreak, today);
    var set = new HashSet<>(days);

    int streak = 0;
    for (LocalDate d = today; ; d = d.minusDays(1)) {
      if (set.contains(d)) streak++;
      else break;
    }

    // 今月の読書日数（distinct days）
    int thisMonthReadingDays = sessionRepo
        .findDistinctSessionDatesByUserIdBetween(userId, startOfMonth, today)
        .size();

    long totalBooks = bookRepo.countByUserId(userId);
    long unreadBooks = bookRepo.countByUserIdAndStatus(userId, "UNREAD");
    long readingBooks = bookRepo.countByUserIdAndStatus(userId, "READING");
    long doneBooks = bookRepo.countByUserIdAndStatus(userId, "DONE");
    long totalSummaries = summaryRepo.countSummariesByUserId(userId);
    
    return new DashboardResponse(
        streak,
        thisMonthMinutes,
        thisMonthReadingDays,
        totalBooks,
        unreadBooks,
        readingBooks,
        doneBooks,
        totalSummaries
    );
  }
}
