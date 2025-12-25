package com.noteplace.readingsummary.summary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.noteplace.readingsummary.summary.domain.Summary;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
  List<Summary> findByBookIdOrderByScopeAscChapterAscIdAsc(Long bookId);

  Optional<Summary> findByBookIdAndScopeAndChapter(Long bookId, String scope, Integer chapter);
  Optional<Summary> findByBookIdAndScopeAndChapterIsNull(Long bookId, String scope);

  Optional<Summary> findByIdAndBookId(Long id, Long bookId);

  @Query("""
		  select count(s)
		  from Summary s, com.noteplace.readingsummary.book.domain.Book b
		  where s.bookId = b.id
		    and b.userId = :userId
		""")
		long countSummariesByUserId(@Param("userId") Long userId);
}
