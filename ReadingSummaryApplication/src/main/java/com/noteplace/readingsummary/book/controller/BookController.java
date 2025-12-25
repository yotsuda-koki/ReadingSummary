package com.noteplace.readingsummary.book.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.noteplace.readingsummary.auth.security.AuthUser;
import com.noteplace.readingsummary.book.domain.Book;
import com.noteplace.readingsummary.book.dto.BookDetailResponse;
import com.noteplace.readingsummary.book.dto.BookResponse;
import com.noteplace.readingsummary.book.dto.CreateBookRequest;
import com.noteplace.readingsummary.book.dto.UpdateBookRequest;
import com.noteplace.readingsummary.book.repository.BookRepository;

@RestController
@RequestMapping("/api/books")
public class BookController {
  private final BookRepository repo;

  public BookController(BookRepository repo) { this.repo = repo; }

  @PostMapping
  public BookResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateBookRequest req) {
    Book b = new Book();
    b.setUserId(user.id());
    b.setTitle(req.title());
    b.setAuthor(req.author());
    b.setLanguage(req.language());
    b.setLevel(req.level());
    b.setStatus(req.status());
    b.setTotalPages(req.totalPages());
    b.setCurrentPage(0);
    repo.save(b);
    return toRes(b);
  }

  @GetMapping
  public List<BookResponse> list(@AuthenticationPrincipal AuthUser user) {
    return repo.findByUserIdOrderByIdDesc(user.id()).stream().map(this::toRes).toList();
  }

  private BookResponse toRes(Book b) {
    return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getLanguage(), b.getLevel(),
      b.getStatus(), b.getTotalPages(), b.getCurrentPage());
  }
  
  @GetMapping("/{id}")
  public BookDetailResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
    Book b = repo.findByIdAndUserId(id, user.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));
    return toDetail(b);
  }

  @PatchMapping("/{id}")
  public BookDetailResponse update(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long id,
      @Valid @RequestBody UpdateBookRequest req
  ) {
    Book b = repo.findByIdAndUserId(id, user.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));

    if (req.status() != null) b.setStatus(req.status());
    if (req.currentPage() != null) b.setCurrentPage(req.currentPage());
    if (req.totalPages() != null) b.setTotalPages(req.totalPages());

    // 追加の整合性チェック（最低限）
    Integer total = b.getTotalPages();
    Integer current = b.getCurrentPage();
    if (total != null && current != null && current > total) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentPage must be <= totalPages");
    }

    repo.save(b);
    return toDetail(b);
  }

  private BookDetailResponse toDetail(Book b) {
    return new BookDetailResponse(
        b.getId(), b.getTitle(), b.getAuthor(), b.getLanguage(), b.getLevel(),
        b.getStatus(), b.getTotalPages(), b.getCurrentPage()
    );
  }

}

