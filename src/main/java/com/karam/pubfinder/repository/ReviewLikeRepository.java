package com.karam.pubfinder.repository;

import com.karam.pubfinder.entity.ReviewLike;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);
    long countByReviewId(Long reviewId);

    // Optimized query to fetch all review IDs liked by a specific user
    @Query("SELECT rl.review.id FROM ReviewLike rl WHERE rl.user.id = :userId")
    Set<Long> findLikedReviewIdsByUserId(Long userId);
}