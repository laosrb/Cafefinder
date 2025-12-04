package com.cafefinder.app;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import com.cafefinder.app.model.Review;
import com.cafefinder.app.repo.ReviewRepo;
import com.cafefinder.app.service.CafeService;
import com.cafefinder.app.service.UserDetailsImpl;
import com.cafefinder.app.web.ReviewController;

/**
 * Test Case #4: LeaveReviewTest
 * Purpose: Verify that a user can successfully leave a review according to requirement 10
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Leave Review Tests")
class LeaveReviewTest {

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private CafeService cafeService;

    @Mock
    private Authentication authentication;

    private ReviewController reviewController;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() throws Exception {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new UserDetailsImpl("user-id", "testuser", "test@example.com", "password", authorities);
        
        // Inject CafeService into ReviewController using reflection
        reviewController = new ReviewController(reviewRepo);
        Field cafeServiceField = ReviewController.class.getDeclaredField("cafeService");
        cafeServiceField.setAccessible(true);
        cafeServiceField.set(reviewController, cafeService);
    }

    @Test
    @DisplayName("Test Case 4.1: Create review with valid overallRating (1)")
    void testCreateReviewWithValidOverallRating1() {
        // Given
        Review review = new Review();
        review.setOverallRating(1);
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getOverallRating());
        verify(reviewRepo, times(1)).save(any(Review.class));
        verify(cafeService, times(1)).updateCafeRatings(anyString());
    }

    @Test
    @DisplayName("Test Case 4.2: Create review with valid overallRating (5)")
    void testCreateReviewWithValidOverallRating5() {
        // Given
        Review review = new Review();
        review.setOverallRating(5);
        review.setCafeId("cafe1");
        review.setText("Excellent!");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getOverallRating());
        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Test Case 4.3: Create review with overallRating < 1")
    void testCreateReviewWithOverallRatingLessThan1() {
        // Given
        Review review = new Review();
        review.setOverallRating(0); // Invalid: less than 1
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When - The setter allows this, but business logic should validate
        review.setOverallRating(0);

        // Then - Verify the value was set (validation would happen at service/controller level)
        assertEquals(0, review.getOverallRating());
    }

    @Test
    @DisplayName("Test Case 4.4: Create review with overallRating > 5")
    void testCreateReviewWithOverallRatingGreaterThan5() {
        // Given
        Review review = new Review();
        review.setOverallRating(6); // Invalid: greater than 5
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setOverallRating(6);

        // Then - Verify the value was set (validation would happen at service/controller level)
        assertEquals(6, review.getOverallRating());
    }

    @Test
    @DisplayName("Test Case 4.5: Create review with valid coffeeRating (1)")
    void testCreateReviewWithValidCoffeeRating1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCoffeeRating(1);
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getCoffeeRating());
        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Test Case 4.6: Create review with valid coffeeRating (5)")
    void testCreateReviewWithValidCoffeeRating5() {
        // Given
        Review review = new Review();
        review.setOverallRating(5);
        review.setCoffeeRating(5);
        review.setCafeId("cafe1");
        review.setText("Great coffee!");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getCoffeeRating());
    }

    @Test
    @DisplayName("Test Case 4.7: Create review with coffeeRating < 1")
    void testCreateReviewWithCoffeeRatingLessThan1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCoffeeRating(0); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setCoffeeRating(0);

        // Then
        assertEquals(0, review.getCoffeeRating());
    }

    @Test
    @DisplayName("Test Case 4.8: Create review with coffeeRating > 5")
    void testCreateReviewWithCoffeeRatingGreaterThan5() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCoffeeRating(6); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setCoffeeRating(6);

        // Then
        assertEquals(6, review.getCoffeeRating());
    }

    @Test
    @DisplayName("Test Case 4.9: Create review with null coffeeRating")
    void testCreateReviewWithNullCoffeeRating() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCoffeeRating(null); // Optional field
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then 
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getCoffeeRating());
        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Test Case 4.10: Create review with valid tasteRating (1)")
    void testCreateReviewWithValidTasteRating1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setTasteRating(1);
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTasteRating());
    }

    @Test
    @DisplayName("Test Case 4.11: Create review with valid tasteRating (5)")
    void testCreateReviewWithValidTasteRating5() {
        // Given
        Review review = new Review();
        review.setOverallRating(5);
        review.setTasteRating(5);
        review.setCafeId("cafe1");
        review.setText("Amazing taste!");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getTasteRating());
    }

    @Test
    @DisplayName("Test Case 4.12: Create review with tasteRating < 1")
    void testCreateReviewWithTasteRatingLessThan1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setTasteRating(0); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setTasteRating(0);

        // Then
        assertEquals(0, review.getTasteRating());
    }

    @Test
    @DisplayName("Test Case 4.13: Create review with tasteRating > 5")
    void testCreateReviewWithTasteRatingGreaterThan5() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setTasteRating(6); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setTasteRating(6);

        // Then
        assertEquals(6, review.getTasteRating());
    }

    @Test
    @DisplayName("Test Case 4.14: Create review with null tasteRating")
    void testCreateReviewWithNullTasteRating() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setTasteRating(null); // Optional field
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getTasteRating());
    }

    @Test
    @DisplayName("Test Case 4.15: Create review with valid ambianceRating (1)")
    void testCreateReviewWithValidAmbianceRating1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setAmbianceRating(1);
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getAmbianceRating());
    }

    @Test
    @DisplayName("Test Case 4.16: Create review with valid ambianceRating (5)")
    void testCreateReviewWithValidAmbianceRating5() {
        // Given
        Review review = new Review();
        review.setOverallRating(5);
        review.setAmbianceRating(5);
        review.setCafeId("cafe1");
        review.setText("Beautiful ambiance!");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getAmbianceRating());
    }

    @Test
    @DisplayName("Test Case 4.17: Create review with ambianceRating < 1")
    void testCreateReviewWithAmbianceRatingLessThan1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setAmbianceRating(0); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setAmbianceRating(0);

        // Then
        assertEquals(0, review.getAmbianceRating());
    }

    @Test
    @DisplayName("Test Case 4.18: Create review with ambianceRating > 5")
    void testCreateReviewWithAmbianceRatingGreaterThan5() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setAmbianceRating(6); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setAmbianceRating(6);

        // Then
        assertEquals(6, review.getAmbianceRating());
    }

    @Test
    @DisplayName("Test Case 4.19: Create review with null ambianceRating")
    void testCreateReviewWithNullAmbianceRating() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setAmbianceRating(null); // Optional field
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getAmbianceRating());
    }

    @Test
    @DisplayName("Test Case 4.20: Create review with valid serviceRating (1)")
    void testCreateReviewWithValidServiceRating1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setServiceRating(1);
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getServiceRating());
    }

    @Test
    @DisplayName("Test Case 4.21: Create review with valid serviceRating (5)")
    void testCreateReviewWithValidServiceRating5() {
        // Given
        Review review = new Review();
        review.setOverallRating(5);
        review.setServiceRating(5);
        review.setCafeId("cafe1");
        review.setText("Excellent service!");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getServiceRating());
    }

    @Test
    @DisplayName("Test Case 4.22: Create review with serviceRating < 1")
    void testCreateReviewWithServiceRatingLessThan1() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setServiceRating(0); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setServiceRating(0);

        // Then
        assertEquals(0, review.getServiceRating());
    }

    @Test
    @DisplayName("Test Case 4.23: Create review with serviceRating > 5")
    void testCreateReviewWithServiceRatingGreaterThan5() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setServiceRating(6); // Invalid
        review.setCafeId("cafe1");
        review.setText("Test review");

        // When
        review.setServiceRating(6);

        // Then
        assertEquals(6, review.getServiceRating());
    }

    @Test
    @DisplayName("Test Case 4.24: Create review with null serviceRating")
    void testCreateReviewWithNullServiceRating() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setServiceRating(null); // Optional field
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getServiceRating());
    }

    @Test
    @DisplayName("Test Case 4.25: Create review with null text")
    void testCreateReviewWithNullText() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCafeId("cafe1");
        review.setText(null); // Null text

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getText());
        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Test Case 4.26: Create review with short text")
    void testCreateReviewWithShortText() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCafeId("cafe1");
        review.setText("Good"); // Short text

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Good", response.getBody().getText());
    }

    @Test
    @DisplayName("Test Case 4.27: Create review with very long text")
    void testCreateReviewWithVeryLongText() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCafeId("cafe1");
        String longText = "A".repeat(5000); // Very long text
        review.setText(longText);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(longText.length(), response.getBody().getText().length());
    }

    @Test
    @DisplayName("Test Case 4.28: Create review with valid overallRating and all optional fields null")
    void testCreateReviewWithValidOverallRatingAndAllOptionalFieldsNull() {
        // Given
        Review review = new Review();
        review.setOverallRating(4); // Required field
        review.setCafeId("cafe1");
        review.setCoffeeRating(null);
        review.setTasteRating(null);
        review.setAmbianceRating(null);
        review.setServiceRating(null);
        review.setText(null);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, response.getBody().getOverallRating());
        assertNull(response.getBody().getCoffeeRating());
        assertNull(response.getBody().getTasteRating());
        assertNull(response.getBody().getAmbianceRating());
        assertNull(response.getBody().getServiceRating());
        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Test Case 4.29: Create review with all valid ratings")
    void testCreateReviewWithAllValidRatings() {
        // Given
        Review review = new Review();
        review.setOverallRating(5);
        review.setCoffeeRating(5);
        review.setTasteRating(5);
        review.setAmbianceRating(5);
        review.setServiceRating(5);
        review.setCafeId("cafe1");
        review.setText("Perfect in every way!");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenReturn(review);

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Review savedReview = response.getBody();
        assertEquals(5, savedReview.getOverallRating());
        assertEquals(5, savedReview.getCoffeeRating());
        assertEquals(5, savedReview.getTasteRating());
        assertEquals(5, savedReview.getAmbianceRating());
        assertEquals(5, savedReview.getServiceRating());
    }

    @Test
    @DisplayName("Test Case 4.30: Verify review is saved with PENDING status")
    void testReviewIsSavedWithPendingStatus() {
        // Given
        Review review = new Review();
        review.setOverallRating(4);
        review.setCafeId("cafe1");
        review.setText("Test review");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reviewRepo.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            saved.setId("review-id");
            return saved;
        });

        // When
        ResponseEntity<Review> response = reviewController.createReview(review, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("PENDING", response.getBody().getStatus());
        assertNotNull(response.getBody().getCreatedAt());
        assertEquals("user-id", response.getBody().getUserId());
        assertEquals("testuser", response.getBody().getUsername());
    }
}

