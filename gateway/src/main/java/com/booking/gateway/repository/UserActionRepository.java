package com.booking.gateway.repository;

import com.booking.gateway.model.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
} 