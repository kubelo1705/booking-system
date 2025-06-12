package com.booking.room.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"booking_id"})
})
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private String bookingId;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    // Current rating statistics
    private double currentAverageRating;
    private int currentTotalReviews;
    private int reviewOrder;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        // Update rating statistics when review is updated
        if (this.room != null) {
            this.currentAverageRating = calculateAverageRating();
            this.currentTotalReviews = calculateTotalReviews();
        }
    }

    private double calculateAverageRating() {
        return this.room.getReviews().stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private int calculateTotalReviews() {
        return this.room.getReviews().size();
    }

    public enum ReviewStatus {
        ACTIVE,
        DELETED
    }
} 