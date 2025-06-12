package com.booking.room.repository;

import com.booking.room.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRoomId(Long roomId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId")
    Double getAverageRatingForRoom(@Param("roomId") Long roomId);
    
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    Optional<Review> findByBookingId(String bookingId);

    @Query("SELECT r FROM Review r WHERE r.room.id = :roomId AND r.userId = :userId")
    List<Review> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") String userId);

    List<Review> findByUserId(String userId);

    @Query("SELECT MAX(r.reviewOrder) FROM Review r WHERE r.room.id = :roomId")
    Optional<Integer> findMaxReviewOrderByRoomId(@Param("roomId") Long roomId);
} 