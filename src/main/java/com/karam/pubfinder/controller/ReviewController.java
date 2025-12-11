package com.karam.pubfinder.controller;

import com.karam.pubfinder.dto.ReviewRequest;
import com.karam.pubfinder.dto.ReviewResponse;
import com.karam.pubfinder.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Helper to extract User ID from Authentication.
     * Returns null if user is anonymous or not authenticated (for public endpoints).
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                Long userId = (Long) authentication.getPrincipal();
                return userId;
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    @PostMapping
    @Operation(summary = "Create a review (requires authentication)",
            description = "Create a new review for a pub. Requires JWT token in Authorization header.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Get all reviews from all users and pubs")
    public ResponseEntity<List<ReviewResponse>> getAllReviews(Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        List<ReviewResponse> reviews = reviewService.getAllReviews(currentUserId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Get a specific review by its ID")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        ReviewResponse review = reviewService.getReviewById(id, currentUserId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/pub/{pubId}")
    @Operation(summary = "Get all reviews for a pub", description = "Get all reviews for a specific pub")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPubId(@PathVariable Long pubId, Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        List<ReviewResponse> reviews = reviewService.getReviewsByPubId(pubId, currentUserId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all reviews by a user", description = "Get all reviews created by a specific user")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable Long userId, Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId, currentUserId);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review (requires authentication)",
            description = "Update your own review. You can only update reviews you created.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewResponse response = reviewService.updateReview(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review (requires authentication)",
            description = "Delete your own review. You can only delete reviews you created.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a review (requires authentication)",
            description = "Like a review. Returns the updated review with like status.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ReviewResponse> likeReview(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewResponse response = reviewService.likeReview(id, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Unlike a review (requires authentication)",
            description = "Remove your like from a review. Returns the updated review with like status.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ReviewResponse> unlikeReview(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewResponse response = reviewService.unlikeReview(id, userId);
        return ResponseEntity.ok(response);
    }
}