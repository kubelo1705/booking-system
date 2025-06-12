package com.booking.room.repository;

import com.booking.room.model.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    List<RoomAvailability> findByRoomId(Long roomId);

    @Query("SELECT ra FROM RoomAvailability ra " +
           "WHERE ra.room.id = :roomId " +
           "AND ra.startTime >= :startTime " +
           "AND ra.endTime <= :endTime")
    List<RoomAvailability> findAvailabilityForRoomInTimeRange(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT ra FROM RoomAvailability ra " +
           "WHERE ra.room.id = :roomId " +
           "AND ra.isAvailable = false " +
           "AND ((ra.startTime <= :endTime AND ra.endTime >= :startTime))")
    List<RoomAvailability> findBookedSlotsForRoom(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
} 