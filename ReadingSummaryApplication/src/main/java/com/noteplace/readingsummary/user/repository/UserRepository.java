package com.noteplace.readingsummary.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.noteplace.readingsummary.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	  Optional<User> findByEmail(String email);
	  boolean existsByEmail(String email);
	}

