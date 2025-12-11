package com.karam.pubfinder.repository;

import com.karam.pubfinder.entity.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPubId(Long pubId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
}