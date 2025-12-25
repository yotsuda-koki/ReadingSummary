package com.noteplace.readingsummary.summary.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "summaries")
@Getter @Setter
public class Summary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "book_id", nullable = false)
  private Long bookId;

  @Column(nullable = false)
  private String scope; // "CHAPTER" or "BOOK"

  private Integer chapter; // CHAPTERの場合に使用（BOOKはnull推奨）

  @Column(name = "content_md", nullable = false, columnDefinition = "text")
  private String contentMd;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
