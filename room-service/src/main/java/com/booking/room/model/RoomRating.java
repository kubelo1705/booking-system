package com.booking.room.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class RoomRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "room_id", unique = true)
    private Room room;

    private double averageRating;
    private int totalReviews;
    private double sumOfRatings;

    public RoomRating(Room room) {
        this.room = room;
        this.averageRating = 0.0;
        this.totalReviews = 0;
        this.sumOfRatings = 0.0;
    }

    public void addRating(double rating) {
        this.totalReviews++;
        this.sumOfRatings += rating;
        this.averageRating = this.sumOfRatings / this.totalReviews;
    }

    public void updateRating(double oldRating, double newRating) {
        this.sumOfRatings = this.sumOfRatings - oldRating + newRating;
        this.averageRating = this.sumOfRatings / this.totalReviews;
    }

    public void removeRating(double rating) {
        this.totalReviews--;
        this.sumOfRatings -= rating;
        this.averageRating = this.totalReviews > 0 ? this.sumOfRatings / this.totalReviews : 0.0;
    }
} 