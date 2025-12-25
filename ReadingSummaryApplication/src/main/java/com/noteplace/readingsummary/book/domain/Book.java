package com.noteplace.readingsummary.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="books")
@Getter @Setter
public class Book {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false)
  private Long userId;

  @Column(nullable=false)
  private String title;

  private String author;
  private String language;
  private String level;

  @Column(nullable=false)
  private String status; // UNREAD/READING/DONE

  private Integer totalPages;

  @Column(nullable=false)
  private Integer currentPage = 0;
}
