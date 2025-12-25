package com.noteplace.readingsummary.session.controller;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.noteplace.readingsummary.auth.security.AuthUser;
import com.noteplace.readingsummary.book.domain.Book;
import com.noteplace.readingsummary.book.repository.BookRepository;
import com.noteplace.readingsummary.session.domain.ReadingSession;
import com.noteplace.readingsummary.session.dto.CreateReadingSessionRequest;
import com.noteplace.readingsummary.session.dto.ReadingSessionResponse;
import com.noteplace.readingsummary.session.repository.ReadingSessionRepository;

@RestController
@RequestMapping("/api/books/{bookId}/sessions")
public class ReadingSessionController {

  private final BookRepository bookRepo;
  private final ReadingSessionRepository sessionRepo;

  public ReadingSessionController(BookRepository bookRepo, ReadingSessionRepository sessionRepo) {
    this.bookRepo = bookRepo;
    this.sessionRepo = sessionRepo;
  }

  @PostMapping
  public ReadingSessionResponse create(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long bookId,
      @Valid @RequestBody CreateReadingSessionRequest req
  ) {
    Book book = bookRepo.findByIdAndUserId(bookId, user.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));

    ReadingSession s = new ReadingSession();
    s.setBookId(bookId);
    s.setSessionDate(req.sessionDate());
    s.setMinutes(req.minutes());
    s.setPagesRead(req.pagesRead());
    s.setMemo(req.memo());
    s.setCreatedAt(Instant.now());
    sessionRepo.save(s);

    // pagesRead が入っていれば currentPage を増やす（Backlog 3の範囲で最低限）
    if (req.pagesRead() != null && req.pagesRead() > 0) {
      int next = (book.getCurrentPage() == null ? 0 : book.getCurrentPage()) + req.pagesRead();
      Integer total = book.getTotalPages();
      if (total != null && next > total) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentPage must be <= totalPages");
      }
      book.setCurrentPage(next);
      bookRepo.save(book);
    }

    return toRes(s);
  }

  @GetMapping
  public List<ReadingSessionResponse> list(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long bookId
  ) {
    bookRepo.findByIdAndUserId(bookId, user.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));

    return sessionRepo.findByBookIdOrderBySessionDateDescCreatedAtDesc(bookId)
        .stream().map(this::toRes).toList();
  }

  private ReadingSessionResponse toRes(ReadingSession s) {
    return new ReadingSessionResponse(
        s.getId(), s.getSessionDate(), s.getMinutes(), s.getPagesRead(), s.getMemo()
    );
  }
}
