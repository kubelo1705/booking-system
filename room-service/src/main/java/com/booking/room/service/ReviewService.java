package com.booking.room.service;

import com.booking.room.model.Review;
import com.booking.room.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final RatingBatchService ratingBatchService;

    public List<Review> getReviewsByRoomId(Long roomId) {
        return reviewRepository.findByRoomId(roomId);
    }

    public List<Review> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    @Transactional
    public Review createReview(Review review) {
        // Set review order
        int lastOrder = reviewRepository.findMaxReviewOrderByRoomId(review.getRoom().getId())
                .orElse(0);
        review.setReviewOrder(lastOrder + 1);
        
        Review savedReview = reviewRepository.save(review);
        
        // Update room rating in batch
        ratingBatchService.updateRoomRating(review.getRoom().getId(), review.getRating(), true);
        
        return savedReview;
    }

    @Transactional
    public Review updateReview(Long id, Review reviewDetails) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Review not found"));
        
        double oldRating = review.getRating();
        review.setRating(reviewDetails.getRating());
        review.setComment(reviewDetails.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        
        // Update room rating in batch
        ratingBatchService.updateRoomRating(review.getRoom().getId(), oldRating, reviewDetails.getRating());
        
        return updatedReview;
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Review not found"));
        
        // Mark as deleted
        review.setStatus(Review.ReviewStatus.DELETED);
        reviewRepository.save(review);
        
        // Update room rating in batch
        ratingBatchService.updateRoomRating(review.getRoom().getId(), review.getRating(), false);
    }

    public double getAverageRoomRating(Long roomId) {
        return ratingBatchService.getRoomRating(roomId).getAverageRating();
    }
} 