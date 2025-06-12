package com.booking.room.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "booking_info", indexes = {
    @Index(name = "idx_booking_room_time", columnList = "room_id,start_time,end_time"),
    @Index(name = "idx_booking_time", columnList = "start_time,end_time"),
    @Index(name = "idx_booking_user", columnList = "user_id")
})
public class BookingInfo {
    @Id
    @Column(name = "booking_id")
    private String bookingId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }
}

enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
} 