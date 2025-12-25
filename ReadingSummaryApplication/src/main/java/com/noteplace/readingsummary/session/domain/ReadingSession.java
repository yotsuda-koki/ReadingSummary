package com.noteplace.readingsummary.session.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reading_sessions")
@Getter @Setter
public class ReadingSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "book_id", nullable = false)
  private Long bookId;

  @Column(name = "session_date", nullable = false)
  private LocalDate sessionDate;

  @Column(nullable = false)
  private Integer minutes;

  @Column(name = "pages_read")
  private Integer pagesRead;

  @Column(columnDefinition = "text")
  private String memo;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
