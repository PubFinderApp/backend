package com.karam.pubfinder.service;

import com.karam.pubfinder.dto.ReviewRequest;
import com.karam.pubfinder.dto.ReviewResponse;
import com.karam.pubfinder.entity.Pub;
import com.karam.pubfinder.entity.Review;
import com.karam.pubfinder.entity.ReviewLike;
import com.karam.pubfinder.entity.User;
import com.karam.pubfinder.repository.PubRepository;
import com.karam.pubfinder.repository.ReviewLikeRepository;
import com.karam.pubfinder.repository.ReviewRepository;
import com.karam.pubfinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PubRepository pubRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pub pub = pubRepository.findById(request.getPubId())
                .orElseThrow(() -> new RuntimeException("Pub not found"));

        Review review = Review.builder()
                .user(user)
                .pub(pub)
                .content(request.getContent())
                .rate(request.getRate())
                .likeCount(0)
                .build();

        review = reviewRepository.save(review);
        updatePubRating(pub.getId());

        // Check if the current user has liked this review (should be false for newly created)
        boolean isLiked = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), userId);
        return mapToResponse(review, isLiked);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews(Long currentUserId) {
        return mapToResponseList(reviewRepository.findAll(), currentUserId);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id, Long currentUserId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Direct check for single review
        boolean isLiked = currentUserId != null &&
                reviewLikeRepository.existsByReviewIdAndUserId(id, currentUserId);

        return mapToResponse(review, isLiked);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByPubId(Long pubId, Long currentUserId) {
        List<Review> reviews = reviewRepository.findByPubId(pubId);
        return mapToResponseList(reviews, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUserId(Long userId, Long currentUserId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return mapToResponseList(reviews, currentUserId);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new RuntimeException("Review not found or you don't have permission to update it"));

        review.setContent(request.getContent());
        review.setRate(request.getRate());

        review = reviewRepository.save(review);
        updatePubRating(review.getPub().getId());

        // Check if the user had previously liked their own review (if permitted)
        boolean isLiked = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);

        return mapToResponse(review, isLiked);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new RuntimeException("Review not found or you don't have permission to delete it"));

        Long pubId = review.getPub().getId();
        reviewRepository.delete(review);
        updatePubRating(pubId);
    }

    @Transactional
    public ReviewResponse likeReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("DEBUG likeReview - reviewId: " + reviewId + ", userId: " + userId);

        boolean alreadyLiked = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);
        System.out.println("DEBUG alreadyLiked: " + alreadyLiked);

        if (alreadyLiked) {
            throw new RuntimeException("You have already liked this review");
        }

        ReviewLike like = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();

        reviewLikeRepository.save(like);
        System.out.println("DEBUG ReviewLike saved");

        review.setLikeCount(review.getLikeCount() + 1);
        review = reviewRepository.save(review);

        // Verify the like was saved
        boolean nowLiked = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);
        System.out.println("DEBUG After save, nowLiked: " + nowLiked);

        // Return the updated review with isLikedByCurrentUser = true
        return mapToResponse(review, true);
    }

    @Transactional
    public ReviewResponse unlikeReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        ReviewLike like = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new RuntimeException("You haven't liked this review"));

        reviewLikeRepository.delete(like);

        review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
        review = reviewRepository.save(review);

        // Return the updated review with isLikedByCurrentUser = false
        return mapToResponse(review, false);
    }

    private void updatePubRating(Long pubId) {
        List<Review> reviews = reviewRepository.findByPubId(pubId);

        if (reviews.isEmpty()) {
            Pub pub = pubRepository.findById(pubId)
                    .orElseThrow(() -> new RuntimeException("Pub not found"));
            pub.setRating(BigDecimal.ZERO);
            pubRepository.save(pub);
            return;
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRate)
                .average()
                .orElse(0.0);

        Pub pub = pubRepository.findById(pubId)
                .orElseThrow(() -> new RuntimeException("Pub not found"));
        pub.setRating(BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP));
        pubRepository.save(pub);
    }

    // --- Helper Methods for Mapping ---

    private List<ReviewResponse> mapToResponseList(List<Review> reviews, Long currentUserId) {
        // Optimization: Fetch all liked IDs for this user in one query
        Set<Long> likedReviewIds = (currentUserId == null)
                ? Collections.emptySet()
                : reviewLikeRepository.findLikedReviewIdsByUserId(currentUserId);

        return reviews.stream()
                .map(review -> mapToResponse(review, likedReviewIds.contains(review.getId())))
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review, boolean isLikedByCurrentUser) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .pubId(review.getPub().getId())
                .pubTitle(review.getPub().getTitle())
                .content(review.getContent())
                .rate(review.getRate())
                .likeCount(review.getLikeCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();
    }
}