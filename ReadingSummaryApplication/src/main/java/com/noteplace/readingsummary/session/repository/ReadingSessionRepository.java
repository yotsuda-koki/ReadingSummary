package com.noteplace.readingsummary.session.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.noteplace.readingsummary.session.domain.ReadingSession;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
  List<ReadingSession> findByBookIdOrderBySessionDateDescCreatedAtDesc(Long bookId);

  @Query("""
		    select coalesce(sum(s.minutes), 0)
		    from ReadingSession s, com.noteplace.readingsummary.book.domain.Book b
		    where s.bookId = b.id
		      and b.userId = :userId
		      and s.sessionDate >= :start
		      and s.sessionDate < :end
		  """)
		  Long sumMinutesByUserIdBetween(
		      @Param("userId") Long userId,
		      @Param("start") LocalDate start,
		      @Param("end") LocalDate end
		  );

		  @Query("""
		    select distinct s.sessionDate
		    from ReadingSession s, com.noteplace.readingsummary.book.domain.Book b
		    where s.bookId = b.id
		      and b.userId = :userId
		      and s.sessionDate >= :start
		      and s.sessionDate <= :end
		    order by s.sessionDate desc
		  """)
		  List<LocalDate> findDistinctSessionDatesByUserIdBetween(
		      @Param("userId") Long userId,
		      @Param("start") LocalDate start,
		      @Param("end") LocalDate end
		  );
}
