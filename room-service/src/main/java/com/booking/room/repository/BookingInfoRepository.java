package com.booking.room.repository;

import com.booking.room.model.BookingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingInfoRepository extends JpaRepository<BookingInfo, String> {
    Optional<BookingInfo> findByBookingId(String bookingId);

    @Query("SELECT b FROM BookingInfo b WHERE b.room.id = :roomId AND b.userId = :userId AND b.status = 'CONFIRMED'")
    Optional<BookingInfo> findActiveBookingByRoomAndUser(@Param("roomId") Long roomId, @Param("userId") String userId);

    List<BookingInfo> findByUserId(String userId);
    
    List<BookingInfo> findByRoomId(Long roomId);
    
    @Query("SELECT b FROM BookingInfo b WHERE b.room.id = :roomId " +
           "AND (b.startTime > :endTime OR b.endTime < :startTime) " +
           "AND b.isAvailable = true")
    List<BookingInfo> findAvailableBookings(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(b) = 0 FROM BookingInfo b WHERE b.room.id = :roomId " +
           "AND NOT (b.startTime > :endTime OR b.endTime < :startTime) " +
           "AND b.isAvailable = false")
    boolean isRoomAvailable(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT b FROM BookingInfo b WHERE b.room.id = :roomId " +
           "AND NOT (b.startTime > :endTime OR b.endTime < :startTime) " +
           "AND b.status = 'CONFIRMED'")
    List<BookingInfo> findConfirmedBookings(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
} 