package com.karam.pubfinder;

import com.karam.pubfinder.dto.PubResponse;
import com.karam.pubfinder.entity.Pub;
import com.karam.pubfinder.repository.PubRepository;
import com.karam.pubfinder.service.PubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PubService Unit Tests")
class PubServiceTest {

    @Mock
    private PubRepository pubRepository;

    @InjectMocks
    private PubService pubService;

    private Pub pub1;
    private Pub pub2;
    private Pub pub3;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        pub1 = Pub.builder()
                .id(1L)
                .title("The Red Lion")
                .shortDescription("Traditional English pub")
                .longDescription("A traditional British pub with great atmosphere and local ales")
                .menuUrl("https://redlion.com/menu")
                .imageUrl("https://redlion.com/image.jpg")
                .rating(new BigDecimal("4.5"))
                .createdAt(testTime.minusDays(10))
                .updatedAt(testTime)
                .build();

        pub2 = Pub.builder()
                .id(2L)
                .title("The Crown & Anchor")
                .shortDescription("Modern gastropub")
                .longDescription("A modern pub with craft beers and gourmet food")
                .menuUrl("https://crownanchor.com/menu")
                .imageUrl("https://crownanchor.com/image.jpg")
                .rating(new BigDecimal("3.8"))
                .createdAt(testTime.minusDays(5))
                .updatedAt(testTime)
                .build();

        pub3 = Pub.builder()
                .id(3L)
                .title("The Old Oak")
                .shortDescription("Cozy neighborhood pub")
                .longDescription("A cozy neighborhood pub with friendly staff and regular events")
                .menuUrl("https://oldoak.com/menu")
                .imageUrl("https://oldoak.com/image.jpg")
                .rating(new BigDecimal("4.2"))
                .createdAt(testTime.minusDays(15))
                .updatedAt(testTime.minusDays(1))
                .build();
    }

    // ==================== getAllPubs Tests ====================

    @Test
    @DisplayName("Should return all pubs without sorting when sortBy is null")
    void getAllPubs_NoSort_ReturnsAllPubs() {
        // Arrange
        List<Pub> pubs = Arrays.asList(pub1, pub2, pub3);
        when(pubRepository.findAll()).thenReturn(pubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs(null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("The Red Lion", result.get(0).getTitle());
        assertEquals("The Crown & Anchor", result.get(1).getTitle());
        assertEquals("The Old Oak", result.get(2).getTitle());

        // Verify interactions
        verify(pubRepository, times(1)).findAll();
        verify(pubRepository, never()).findAllByOrderByRatingAsc();
        verify(pubRepository, never()).findAllByOrderByRatingDesc();
    }

    @Test
    @DisplayName("Should return pubs sorted by rating in ascending order")
    void getAllPubs_SortByAsc_ReturnsSortedPubsAscending() {
        // Arrange - sorted: 3.8, 4.2, 4.5
        List<Pub> sortedPubs = Arrays.asList(pub2, pub3, pub1);
        when(pubRepository.findAllByOrderByRatingAsc()).thenReturn(sortedPubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs("asc");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify order
        assertThat(result.get(0).getRating()).isEqualByComparingTo(new BigDecimal("3.8"));
        assertThat(result.get(1).getRating()).isEqualByComparingTo(new BigDecimal("4.2"));
        assertThat(result.get(2).getRating()).isEqualByComparingTo(new BigDecimal("4.5"));

        assertEquals("The Crown & Anchor", result.get(0).getTitle());
        assertEquals("The Old Oak", result.get(1).getTitle());
        assertEquals("The Red Lion", result.get(2).getTitle());

        // Verify interactions
        verify(pubRepository, times(1)).findAllByOrderByRatingAsc();
        verify(pubRepository, never()).findAll();
        verify(pubRepository, never()).findAllByOrderByRatingDesc();
    }

    @Test
    @DisplayName("Should return pubs sorted by rating in descending order")
    void getAllPubs_SortByDesc_ReturnsSortedPubsDescending() {
        // Arrange - sorted: 4.5, 4.2, 3.8
        List<Pub> sortedPubs = Arrays.asList(pub1, pub3, pub2);
        when(pubRepository.findAllByOrderByRatingDesc()).thenReturn(sortedPubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs("desc");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify order
        assertThat(result.get(0).getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(result.get(1).getRating()).isEqualByComparingTo(new BigDecimal("4.2"));
        assertThat(result.get(2).getRating()).isEqualByComparingTo(new BigDecimal("3.8"));

        assertEquals("The Red Lion", result.get(0).getTitle());
        assertEquals("The Old Oak", result.get(1).getTitle());
        assertEquals("The Crown & Anchor", result.get(2).getTitle());

        // Verify interactions
        verify(pubRepository, times(1)).findAllByOrderByRatingDesc();
        verify(pubRepository, never()).findAll();
        verify(pubRepository, never()).findAllByOrderByRatingAsc();
    }

    @Test
    @DisplayName("Should handle case-insensitive 'ASC' sorting")
    void getAllPubs_UppercaseASC_ReturnsSortedPubs() {
        // Arrange
        List<Pub> sortedPubs = Arrays.asList(pub2, pub3, pub1);
        when(pubRepository.findAllByOrderByRatingAsc()).thenReturn(sortedPubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs("ASC");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(pubRepository, times(1)).findAllByOrderByRatingAsc();
    }

    @Test
    @DisplayName("Should handle case-insensitive 'DESC' sorting")
    void getAllPubs_UppercaseDESC_ReturnsSortedPubs() {
        // Arrange
        List<Pub> sortedPubs = Arrays.asList(pub1, pub3, pub2);
        when(pubRepository.findAllByOrderByRatingDesc()).thenReturn(sortedPubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs("DESC");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(pubRepository, times(1)).findAllByOrderByRatingDesc();
    }

    @Test
    @DisplayName("Should return unsorted pubs for invalid sort parameter")
    void getAllPubs_InvalidSortParameter_ReturnsUnsortedPubs() {
        // Arrange
        List<Pub> pubs = Arrays.asList(pub1, pub2, pub3);
        when(pubRepository.findAll()).thenReturn(pubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs("invalid");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(pubRepository, times(1)).findAll();
        verify(pubRepository, never()).findAllByOrderByRatingAsc();
        verify(pubRepository, never()).findAllByOrderByRatingDesc();
    }

    @Test
    @DisplayName("Should return empty list when no pubs exist")
    void getAllPubs_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(pubRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PubResponse> result = pubService.getAllPubs(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());

        verify(pubRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle empty string as sort parameter")
    void getAllPubs_EmptyStringSortParameter_ReturnsUnsortedPubs() {
        // Arrange
        List<Pub> pubs = Arrays.asList(pub1, pub2, pub3);
        when(pubRepository.findAll()).thenReturn(pubs);

        // Act
        List<PubResponse> result = pubService.getAllPubs("");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(pubRepository, times(1)).findAll();
    }

    // ==================== getPubById Tests ====================

    @Test
    @DisplayName("Should return pub by ID successfully")
    void getPubById_ValidId_ReturnsPub() {
        // Arrange
        when(pubRepository.findById(1L)).thenReturn(Optional.of(pub1));

        // Act
        PubResponse result = pubService.getPubById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("The Red Lion", result.getTitle());
        assertEquals("Traditional English pub", result.getShortDescription());
        assertEquals("A traditional British pub with great atmosphere and local ales",
                result.getLongDescription());
        assertEquals("https://redlion.com/menu", result.getMenuUrl());
        assertEquals("https://redlion.com/image.jpg", result.getImageUrl());
        assertThat(result.getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(pubRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when pub is not found by ID")
    void getPubById_InvalidId_ThrowsException() {
        // Arrange
        Long invalidId = 999L;
        when(pubRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pubService.getPubById(invalidId));

        assertEquals("Pub not found with id: 999", exception.getMessage());
        verify(pubRepository, times(1)).findById(invalidId);
    }

    @Test
    @DisplayName("Should throw exception for null ID")
    void getPubById_NullId_ThrowsException() {
        // Arrange
        when(pubRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> pubService.getPubById(null));
        verify(pubRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should return pub with zero rating")
    void getPubById_PubWithZeroRating_ReturnsPub() {
        // Arrange
        pub1.setRating(BigDecimal.ZERO);
        when(pubRepository.findById(1L)).thenReturn(Optional.of(pub1));

        // Act
        PubResponse result = pubService.getPubById(1L);

        // Assert
        assertNotNull(result);
        assertThat(result.getRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return pub with null optional fields")
    void getPubById_PubWithNullFields_ReturnsPub() {
        // Arrange
        pub1.setMenuUrl(null);
        pub1.setImageUrl(null);
        when(pubRepository.findById(1L)).thenReturn(Optional.of(pub1));

        // Act
        PubResponse result = pubService.getPubById(1L);

        // Assert
        assertNotNull(result);
        assertNull(result.getMenuUrl());
        assertNull(result.getImageUrl());
        assertEquals("The Red Lion", result.getTitle());
    }

    // ==================== Response Mapping Tests ====================

    @Test
    @DisplayName("Should correctly map all pub fields to response")
    void mapToResponse_AllFields_CorrectlyMapped() {
        // Arrange
        when(pubRepository.findById(1L)).thenReturn(Optional.of(pub1));

        // Act
        PubResponse result = pubService.getPubById(1L);

        // Assert - verify all fields are mapped correctly
        assertAll("Pub Response Mapping",
                () -> assertEquals(pub1.getId(), result.getId()),
                () -> assertEquals(pub1.getTitle(), result.getTitle()),
                () -> assertEquals(pub1.getShortDescription(), result.getShortDescription()),
                () -> assertEquals(pub1.getLongDescription(), result.getLongDescription()),
                () -> assertEquals(pub1.getMenuUrl(), result.getMenuUrl()),
                () -> assertEquals(pub1.getImageUrl(), result.getImageUrl()),
                () -> assertThat(result.getRating()).isEqualByComparingTo(pub1.getRating()),
                () -> assertEquals(pub1.getCreatedAt(), result.getCreatedAt()),
                () -> assertEquals(pub1.getUpdatedAt(), result.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("Should handle pubs with very long descriptions")
    void getAllPubs_PubWithLongDescription_HandlesCorrectly() {
        // Arrange
        String longDescription = "A".repeat(1000);
        pub1.setLongDescription(longDescription);
        when(pubRepository.findAll()).thenReturn(Collections.singletonList(pub1));

        // Act
        List<PubResponse> result = pubService.getAllPubs(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(longDescription, result.get(0).getLongDescription());
    }

    @Test
    @DisplayName("Should verify repository is called only once for each operation")
    void getAllPubs_VerifyRepositoryInteraction_CalledOnce() {
        // Arrange
        when(pubRepository.findAll()).thenReturn(Arrays.asList(pub1, pub2));

        // Act
        pubService.getAllPubs(null);

        // Assert
        verify(pubRepository, times(1)).findAll();
        verifyNoMoreInteractions(pubRepository);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle mixed case sort parameters")
    void getAllPubs_MixedCaseSortParameter_HandlesCorrectly() {
        // Arrange
        List<Pub> sortedPubs = Arrays.asList(pub2, pub3, pub1);
        when(pubRepository.findAllByOrderByRatingAsc()).thenReturn(sortedPubs);

        // Act
        List<PubResponse> resultAsc = pubService.getAllPubs("AsC");
        List<PubResponse> resultDesc = pubService.getAllPubs("DeSc");

        // Assert
        assertNotNull(resultAsc);
        verify(pubRepository).findAllByOrderByRatingAsc();
    }

    @Test
    @DisplayName("Should handle pub with decimal rating precision")
    void getPubById_PubWithPreciseRating_ReturnsExactRating() {
        // Arrange
        pub1.setRating(new BigDecimal("4.567"));
        when(pubRepository.findById(1L)).thenReturn(Optional.of(pub1));

        // Act
        PubResponse result = pubService.getPubById(1L);

        // Assert
        assertNotNull(result);
        assertThat(result.getRating()).isEqualByComparingTo(new BigDecimal("4.567"));
    }
}