package com.booking.room.repository;

import com.booking.room.model.RatingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingStatsRepository extends JpaRepository<RatingStats, Long> {
    Optional<RatingStats> findByRoomId(Long roomId);
} 