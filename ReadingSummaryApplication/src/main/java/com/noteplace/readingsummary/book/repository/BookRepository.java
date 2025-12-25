package com.noteplace.readingsummary.book.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.noteplace.readingsummary.book.domain.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
	  List<Book> findByUserIdOrderByIdDesc(Long userId);
	  
	  Optional<Book> findByIdAndUserId(Long id, Long userId);	

	  long countByUserId(Long userId);
	  long countByUserIdAndStatus(Long userId, String status);
}