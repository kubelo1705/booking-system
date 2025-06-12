package com.booking.room.controller;

import com.booking.room.model.Review;
import com.booking.room.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private static final String USER_ID_HEADER = "X-User-ID";

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Review>> getReviewsByRoom(@PathVariable Long roomId) {
        List<Review> reviews = reviewService.getReviewsByRoomId(roomId);
        return reviews.isEmpty() ?
            ResponseEntity.notFound().build() :
            ResponseEntity.ok(reviews);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Review>> getReviewsByCurrentUser(
            @RequestHeader(USER_ID_HEADER) String userId) {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return reviews.isEmpty() ?
            ResponseEntity.notFound().build() :
            ResponseEntity.ok(reviews);
    }

    @GetMapping("/room/{roomId}/average-rating")
    public ResponseEntity<Double> getAverageRoomRating(@PathVariable Long roomId) {
        double rating = reviewService.getAverageRoomRating(roomId);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody Review review,
            @RequestHeader(USER_ID_HEADER) String userId) {
        try {
            review.setUserId(userId);
            Review createdReview = reviewService.createReview(review);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdReview);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody Review review,
            @RequestHeader(USER_ID_HEADER) String userId) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            review.setUserId(userId);
            Review updatedReview = reviewService.updateReview(id, review);
            return updatedReview != null ? 
                    ResponseEntity.ok(updatedReview) : 
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
} 