package com.karam.pubfinder;

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
import com.karam.pubfinder.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PubRepository pubRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Pub pub;
    private Review review;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        pub = Pub.builder()
                .id(1L)
                .title("The Red Lion")
                .rating(BigDecimal.ZERO)
                .build();

        review = Review.builder()
                .id(1L)
                .user(user)
                .pub(pub)
                .content("Great pub!")
                .rate(5)
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reviewRequest = ReviewRequest.builder()
                .pubId(1L)
                .content("Great pub!")
                .rate(5)
                .build();
    }

    @Test
    void createReview_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(pubRepository.findById(anyLong())).thenReturn(Optional.of(pub));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(reviewRepository.findByPubId(anyLong())).thenReturn(Arrays.asList(review));

        // Act
        ReviewResponse result = reviewService.createReview(reviewRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Great pub!", result.getContent());
        assertEquals(5, result.getRate());
        assertEquals(0, result.getLikeCount());
        assertFalse(result.isLikedByCurrentUser());

        verify(userRepository).findById(1L);
        verify(pubRepository, times(2)).findById(1L); // Once for review, once for rating update
        verify(reviewRepository).save(any(Review.class));
        verify(pubRepository).save(any(Pub.class)); // Rating update
    }

    @Test
    void createReview_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.createReview(reviewRequest, 1L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_PubNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(pubRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.createReview(reviewRequest, 1L));

        assertEquals("Pub not found", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(pubRepository).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getAllReviews_WithLikes() {
        // Arrange
        Review review2 = Review.builder()
                .id(2L)
                .user(user)
                .pub(pub)
                .content("Nice place")
                .rate(4)
                .likeCount(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review, review2));
        when(reviewLikeRepository.findLikedReviewIdsByUserId(anyLong()))
                .thenReturn(new HashSet<>(Arrays.asList(1L)));

        // Act
        List<ReviewResponse> result = reviewService.getAllReviews(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isLikedByCurrentUser());
        assertFalse(result.get(1).isLikedByCurrentUser());

        verify(reviewRepository).findAll();
        verify(reviewLikeRepository).findLikedReviewIdsByUserId(1L);
    }

    @Test
    void getAllReviews_NoCurrentUser() {
        // Arrange
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review));

        // Act
        List<ReviewResponse> result = reviewService.getAllReviews(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isLikedByCurrentUser());

        verify(reviewRepository).findAll();
        verify(reviewLikeRepository, never()).findLikedReviewIdsByUserId(anyLong());
    }

    @Test
    void getReviewById_Success() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(true);

        // Act
        ReviewResponse result = reviewService.getReviewById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.isLikedByCurrentUser());

        verify(reviewRepository).findById(1L);
        verify(reviewLikeRepository).existsByReviewIdAndUserId(1L, 1L);
    }

    @Test
    void getReviewById_NotFound_ThrowsException() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.getReviewById(999L, 1L));

        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository).findById(999L);
    }

    @Test
    void getReviewsByPubId_Success() {
        // Arrange
        when(reviewRepository.findByPubId(anyLong())).thenReturn(Arrays.asList(review));
        when(reviewLikeRepository.findLikedReviewIdsByUserId(anyLong()))
                .thenReturn(Collections.emptySet());

        // Act
        List<ReviewResponse> result = reviewService.getReviewsByPubId(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getPubId());

        verify(reviewRepository).findByPubId(1L);
    }

    @Test
    void getReviewsByUserId_Success() {
        // Arrange
        when(reviewRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(review));
        when(reviewLikeRepository.findLikedReviewIdsByUserId(anyLong()))
                .thenReturn(Collections.emptySet());

        // Act
        List<ReviewResponse> result = reviewService.getReviewsByUserId(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());

        verify(reviewRepository).findByUserId(1L);
    }

    @Test
    void updateReview_Success() {
        // Arrange
        ReviewRequest updateRequest = ReviewRequest.builder()
                .pubId(1L)
                .content("Updated review")
                .rate(4)
                .build();

        when(reviewRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(reviewRepository.findByPubId(anyLong())).thenReturn(Arrays.asList(review));
        when(pubRepository.findById(anyLong())).thenReturn(Optional.of(pub));

        // Act
        ReviewResponse result = reviewService.updateReview(1L, updateRequest, 1L);

        // Assert
        assertNotNull(result);
        verify(reviewRepository).findByIdAndUserId(1L, 1L);
        verify(reviewRepository).save(any(Review.class));
        verify(pubRepository).save(any(Pub.class)); // Rating update
    }

    @Test
    void updateReview_NotFoundOrNoPermission_ThrowsException() {
        // Arrange
        when(reviewRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.updateReview(1L, reviewRequest, 1L));

        assertTrue(exception.getMessage().contains("Review not found or you don't have permission"));
        verify(reviewRepository).findByIdAndUserId(1L, 1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_Success() {
        // Arrange
        when(reviewRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(review));
        when(reviewRepository.findByPubId(anyLong())).thenReturn(Collections.emptyList());
        when(pubRepository.findById(anyLong())).thenReturn(Optional.of(pub));

        // Act
        reviewService.deleteReview(1L, 1L);

        // Assert
        verify(reviewRepository).findByIdAndUserId(1L, 1L);
        verify(reviewRepository).delete(review);
        verify(pubRepository).save(any(Pub.class)); // Rating update
    }

    @Test
    void deleteReview_NotFoundOrNoPermission_ThrowsException() {
        // Arrange
        when(reviewRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.deleteReview(1L, 1L));

        assertTrue(exception.getMessage().contains("Review not found or you don't have permission"));
        verify(reviewRepository).findByIdAndUserId(1L, 1L);
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void likeReview_Success() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        ReviewResponse result = reviewService.likeReview(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isLikedByCurrentUser());

        verify(reviewRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(reviewRepository).save(review);
    }

    @Test
    void likeReview_AlreadyLiked_ThrowsException() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.likeReview(1L, 1L));

        assertEquals("You have already liked this review", exception.getMessage());
        verify(reviewLikeRepository, never()).save(any(ReviewLike.class));
    }

    @Test
    void unlikeReview_Success() {
        // Arrange
        ReviewLike like = ReviewLike.builder()
                .id(1L)
                .review(review)
                .user(user)
                .build();

        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(like));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        ReviewResponse result = reviewService.unlikeReview(1L, 1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isLikedByCurrentUser());

        verify(reviewRepository).findById(1L);
        verify(reviewLikeRepository).findByReviewIdAndUserId(1L, 1L);
        verify(reviewLikeRepository).delete(like);
        verify(reviewRepository).save(review);
    }

    @Test
    void unlikeReview_NotLiked_ThrowsException() {
        // Arrange
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.unlikeReview(1L, 1L));

        assertEquals("You haven't liked this review", exception.getMessage());
        verify(reviewLikeRepository, never()).delete(any(ReviewLike.class));
    }

    @Test
    void updatePubRating_MultipleReviews_CalculatesAverage() {
        // Arrange
        Review review2 = Review.builder()
                .id(2L)
                .user(user)
                .pub(pub)
                .rate(3)
                .build();

        Review review3 = Review.builder()
                .id(3L)
                .user(user)
                .pub(pub)
                .rate(4)
                .build();

        when(reviewRepository.findByPubId(anyLong())).thenReturn(Arrays.asList(review, review2, review3));
        when(pubRepository.findById(anyLong())).thenReturn(Optional.of(pub));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewLikeRepository.existsByReviewIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        // Act
        reviewService.createReview(reviewRequest, 1L);

        // Assert
        verify(pubRepository).save(argThat(p ->
                p.getRating().compareTo(new BigDecimal("4.0")) == 0
        ));
    }
}