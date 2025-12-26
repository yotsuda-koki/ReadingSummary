package com.noteplace.readingsummary.summary.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.noteplace.readingsummary.auth.security.AuthUser;
import com.noteplace.readingsummary.book.repository.BookRepository;
import com.noteplace.readingsummary.summary.domain.Summary;
import com.noteplace.readingsummary.summary.dto.CreateSummaryRequest;
import com.noteplace.readingsummary.summary.dto.SummaryResponse;
import com.noteplace.readingsummary.summary.repository.SummaryRepository;

@RestController
@RequestMapping("/api/books/{bookId}/summaries")
public class SummaryController {

  private final BookRepository bookRepo;
  private final SummaryRepository summaryRepo;

  public SummaryController(BookRepository bookRepo, SummaryRepository summaryRepo) {
    this.bookRepo = bookRepo;
    this.summaryRepo = summaryRepo;
  }

  @GetMapping
  public List<SummaryResponse> list(@AuthenticationPrincipal AuthUser user, @PathVariable Long bookId) {
    ensureOwnBook(user, bookId);
    return summaryRepo.findByBookIdOrderByScopeAscChapterAscIdAsc(bookId)
        .stream().map(this::toRes).toList();
  }

  @PostMapping
  public SummaryResponse upsert(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long bookId,
      @Valid @RequestBody CreateSummaryRequest req
  ) {
    ensureOwnBook(user, bookId);

    String scope = normalizeScope(req.scope());
    Integer chapter = req.chapter();

    if ("CHAPTER".equals(scope)) {
      if (chapter == null || chapter < 1) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chapter is required and must be >= 1 for CHAPTER scope");
      }
    } else { // BOOK
      chapter = null; // BOOK は chapter を null に寄せる
    }

    Optional<Summary> existing = ("CHAPTER".equals(scope))
        ? summaryRepo.findByBookIdAndScopeAndChapter(bookId, scope, chapter)
        : summaryRepo.findByBookIdAndScopeAndChapterIsNull(bookId, scope);

    Instant now = Instant.now();
    Summary s = existing.orElseGet(Summary::new);
    if (s.getId() == null) {
      s.setBookId(bookId);
      s.setCreatedAt(now);
    }
    s.setScope(scope);
    s.setChapter(chapter);
    s.setContentMd(req.contentMd());
    s.setUpdatedAt(now);

    summaryRepo.save(s);
    return toRes(s);
  }

  private void ensureOwnBook(AuthUser user, Long bookId) {
    bookRepo.findByIdAndUserId(bookId, user.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));
  }

  private String normalizeScope(String scope) {
    if (scope == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope is required");
    String s = scope.trim().toUpperCase();
    if (!s.equals("CHAPTER") && !s.equals("BOOK")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must be CHAPTER or BOOK");
    }
    return s;
  }

  private SummaryResponse toRes(Summary s) {
    return new SummaryResponse(
        s.getId(), s.getScope(), s.getChapter(), s.getContentMd(), s.getCreatedAt(), s.getUpdatedAt()
    );
  }
  
  @DeleteMapping("/{summaryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long bookId,
      @PathVariable Long summaryId
  ) {
    ensureOwnBook(user, bookId);

    Summary s = summaryRepo.findByIdAndBookId(summaryId, bookId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "summary not found"));

    summaryRepo.delete(s);
  }
}
