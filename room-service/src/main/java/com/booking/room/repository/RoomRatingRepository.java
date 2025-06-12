package com.booking.room.repository;

import com.booking.room.model.RoomRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRatingRepository extends JpaRepository<RoomRating, Long> {
    Optional<RoomRating> findByRoomId(Long roomId);
} 