package com.booking.room.repository;

import com.booking.room.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r " +
           "WHERE r.hotel.city = :city " +
           "AND r.price BETWEEN :minPrice AND :maxPrice " +
           "AND r.capacity >= :minCapacity " +
           "AND (:roomType IS NULL OR r.type = :roomType) " +
           "AND NOT EXISTS (SELECT b FROM BookingInfo b " +
           "WHERE b.room = r " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime)))")
    Page<Room> findAvailableRooms(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minCapacity") Integer minCapacity,
            @Param("roomType") String roomType,
            @Param("city") String city,
            Pageable pageable);

    @Query("SELECT r FROM Room r " +
           "WHERE (SELECT AVG(rev.rating) FROM Review rev WHERE rev.room = r) >= :minRating " +
           "ORDER BY (SELECT AVG(rev.rating) FROM Review rev WHERE rev.room = r) DESC")
    Page<Room> findRoomsByMinRating(
            @Param("minRating") Double minRating,
            Pageable pageable);

    @Query("SELECT r FROM Room r " +
           "WHERE (SELECT AVG(rev.rating) FROM Review rev WHERE rev.room = r) >= :minRating " +
           "AND r.hotel.city = :city " +
           "ORDER BY (SELECT AVG(rev.rating) FROM Review rev WHERE rev.room = r) DESC")
    Page<Room> findRoomsByMinRatingAndCity(
            @Param("minRating") Double minRating,
            @Param("city") String city,
            Pageable pageable);

    List<Room> findByHotelId(Long hotelId);
} 