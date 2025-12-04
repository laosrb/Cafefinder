package com.cafefinder.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.cafefinder.app.model.Cafe;
import com.cafefinder.app.model.MenuItem;
import com.cafefinder.app.model.Review;
import com.cafefinder.app.repo.CafeRepo;
import com.cafefinder.app.repo.ReviewRepo;
import com.cafefinder.app.service.CafeService;
import com.cafefinder.app.web.CafeController;
import com.cafefinder.app.web.ReviewController;

/**
 * Test Case #2: CafeOverviewTest
 * Purpose: Verify that a user can successfully view a cafe overview according to requirement 2
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Cafe Overview Tests")
class CafeOverviewTest {

    @Mock
    private CafeRepo cafeRepo;

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private CafeService cafeService;

    private CafeController cafeController;
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        cafeController = new CafeController(cafeRepo, cafeService);
        reviewController = new ReviewController(reviewRepo);
    }

    @Test
    @DisplayName("Test Case 2.1: View cafe with complete profile")
    void testViewCafeWithCompleteProfile() {
        // Given
        String cafeId = "cafe1";
        Cafe completeCafe = createCompleteCafe(cafeId);
        List<Review> reviews = createTestReviews(cafeId);

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(completeCafe));
        when(reviewRepo.findByCafeIdAndStatusOrderByCreatedAtDesc(cafeId, "APPROVED")).thenReturn(reviews);

        // When
        ResponseEntity<Cafe> response = cafeController.getPublic(cafeId);
        List<Review> cafeReviews = reviewController.getReviewsByCafe(cafeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Cafe cafe = response.getBody();
        
        // Verify all required fields are present
        assertNotNull(cafe.getName());
        assertNotNull(cafe.getAddress());
        assertNotNull(cafe.getHours());
        assertFalse(cafe.getHours().isEmpty());
        assertNotNull(cafe.getMenuItems());
        assertFalse(cafe.getMenuItems().isEmpty());
        
        // Verify reviews are returned
        assertNotNull(cafeReviews);
        assertFalse(cafeReviews.isEmpty());
    }

    @Test
    @DisplayName("Test Case 2.2: View cafe with partial profile (missing hours)")
    void testViewCafeWithMissingHours() {
        // Given
        String cafeId = "cafe2";
        Cafe partialCafe = createPartialCafe(cafeId);
        partialCafe.setHours(null); // Missing hours

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(partialCafe));

        // When
        ResponseEntity<Cafe> response = cafeController.getPublic(cafeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Cafe cafe = response.getBody();
        
        // Should still load but hours may be null
        assertNull(cafe.getHours());
        // Other fields should still be present
        assertNotNull(cafe.getName());
        assertNotNull(cafe.getAddress());
    }

    @Test
    @DisplayName("Test Case 2.3: View cafe with missing menu")
    void testViewCafeWithMissingMenu() {
        // Given
        String cafeId = "cafe3";
        Cafe partialCafe = createPartialCafe(cafeId);
        partialCafe.setMenuItems(null); // Missing menu

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(partialCafe));

        // When
        ResponseEntity<?> menuResponse = cafeController.getMenu(cafeId);

        // Then
        assertEquals(HttpStatus.OK, menuResponse.getStatusCode());
        // Should handle null menu gracefully
    }

    @Test
    @DisplayName("Test Case 2.4: View cafe with no reviews")
    void testViewCafeWithNoReviews() {
        // Given
        String cafeId = "cafe4";

        when(reviewRepo.findByCafeIdAndStatusOrderByCreatedAtDesc(cafeId, "APPROVED")).thenReturn(new ArrayList<>());

        // When
        List<Review> reviews = reviewController.getReviewsByCafe(cafeId);

        // Then
        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
        // Should return empty list, not null or error
    }

    @Test
    @DisplayName("Test Case 2.5: View nonexistent cafe")
    void testViewNonexistentCafe() {
        // Given
        String nonExistentId = "nonexistent-cafe-id";

        when(cafeRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Cafe> response = cafeController.getPublic(nonExistentId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test Case 2.6: View cafe menu endpoint")
    void testViewCafeMenu() {
        // Given
        String cafeId = "cafe1";
        Cafe cafe = createCompleteCafe(cafeId);

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(cafe));

        // When
        ResponseEntity<?> response = cafeController.getMenu(cafeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
    }

    @Test
    @DisplayName("Test Case 2.7: View cafe with missing location")
    void testViewCafeWithMissingLocation() {
        // Given
        String cafeId = "cafe5";
        Cafe cafe = createPartialCafe(cafeId);
        cafe.setAddress(null);
        cafe.setCity(null);
        cafe.setLatitude(0.0);
        cafe.setLongitude(0.0);

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(cafe));

        // When
        ResponseEntity<Cafe> response = cafeController.getPublic(cafeId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should still load but location fields may be null or default
    }

    @Test
    @DisplayName("Test Case 2.8: Select different cafe from search results")
    void testSelectDifferentCafe() {
        // Given
        String cafeId1 = "cafe1";
        String cafeId2 = "cafe2";
        Cafe cafe1 = createCompleteCafe(cafeId1);
        Cafe cafe2 = createCompleteCafe(cafeId2);

        when(cafeRepo.findById(cafeId1)).thenReturn(Optional.of(cafe1));
        when(cafeRepo.findById(cafeId2)).thenReturn(Optional.of(cafe2));

        // When
        ResponseEntity<Cafe> response1 = cafeController.getPublic(cafeId1);
        ResponseEntity<Cafe> response2 = cafeController.getPublic(cafeId2);

        // Then
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotEquals(response1.getBody().getId(), response2.getBody().getId());
    }

    @Test
    @DisplayName("Test Case 2.9: View cafe with missing reviews but has other data")
    void testViewCafeWithMissingReviewsButHasData() {
        // Given
        String cafeId = "cafe6";
        Cafe cafe = createCompleteCafe(cafeId);

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(cafe));
        when(reviewRepo.findByCafeIdAndStatusOrderByCreatedAtDesc(cafeId, "APPROVED")).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<Cafe> cafeResponse = cafeController.getPublic(cafeId);
        List<Review> reviews = reviewController.getReviewsByCafe(cafeId);

        // Then
        assertEquals(HttpStatus.OK, cafeResponse.getStatusCode());
        assertNotNull(cafeResponse.getBody());
        assertTrue(reviews.isEmpty());
        // Cafe should still have other information like hours, menu, etc.
        assertNotNull(cafeResponse.getBody().getHours());
        assertNotNull(cafeResponse.getBody().getMenuItems());
    }

    @Test
    @DisplayName("Test Case 2.10: Verify cafe overview shows all required information")
    void testCafeOverviewShowsAllRequiredInfo() {
        // Given
        String cafeId = "cafe1";
        Cafe cafe = createCompleteCafe(cafeId);
        List<Review> reviews = createTestReviews(cafeId);

        when(cafeRepo.findById(cafeId)).thenReturn(Optional.of(cafe));
        when(reviewRepo.findByCafeIdAndStatusOrderByCreatedAtDesc(cafeId, "APPROVED")).thenReturn(reviews);

        // When
        ResponseEntity<Cafe> response = cafeController.getPublic(cafeId);
        List<Review> cafeReviews = reviewController.getReviewsByCafe(cafeId);

        // Then
        Cafe retrievedCafe = response.getBody();
        assertNotNull(retrievedCafe);
        
        // Verify all key information is present
        assertNotNull(retrievedCafe.getName(), "Cafe name should be present");
        assertNotNull(retrievedCafe.getAddress(), "Cafe address should be present");
        assertNotNull(retrievedCafe.getHours(), "Cafe hours should be present");
        assertNotNull(retrievedCafe.getMenuItems(), "Cafe menu should be present");
        assertNotNull(cafeReviews, "Reviews list should not be null");
    }

    // Helper methods
    private Cafe createCompleteCafe(String id) {
        Cafe cafe = new Cafe();
        cafe.setId(id);
        cafe.setName("Complete Test Cafe");
        cafe.setDescription("A complete test cafe");
        cafe.setAddress("123 Test Street");
        cafe.setCity("Atlanta");
        cafe.setState("GA");
        cafe.setZipCode("30301");
        cafe.setPhone("555-1234");
        cafe.setWebsite("https://testcafe.com");
        cafe.setLatitude(33.7490);
        cafe.setLongitude(-84.3880);
        
        // Set hours
        Map<Integer, String> hours = new HashMap<>();
        hours.put(0, "8:00-18:00"); // Sunday
        hours.put(1, "7:00-20:00"); // Monday
        hours.put(2, "7:00-20:00"); // Tuesday
        hours.put(3, "7:00-20:00"); // Wednesday
        hours.put(4, "7:00-20:00"); // Thursday
        hours.put(5, "7:00-22:00"); // Friday
        hours.put(6, "8:00-22:00"); // Saturday
        cafe.setHours(hours);
        
        // Set menu items
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem item1 = new MenuItem();
        item1.setName("Espresso");
        item1.setPrice(3.50);
        menuItems.add(item1);
        
        MenuItem item2 = new MenuItem();
        item2.setName("Latte");
        item2.setPrice(4.50);
        menuItems.add(item2);
        
        cafe.setMenuItems(menuItems);
        
        cafe.setWifi(true);
        cafe.setSeating(true);
        cafe.setWorkFriendly(true);
        cafe.setAvgRating(4.5);
        cafe.setReviewsCount(10);
        
        return cafe;
    }

    private Cafe createPartialCafe(String id) {
        Cafe cafe = new Cafe();
        cafe.setId(id);
        cafe.setName("Partial Test Cafe");
        cafe.setAddress("456 Partial Street");
        cafe.setCity("Atlanta");
        cafe.setState("GA");
        cafe.setLatitude(33.7500);
        cafe.setLongitude(-84.3890);
        cafe.setAvgRating(3.5);
        return cafe;
    }

    private List<Review> createTestReviews(String cafeId) {
        Review review1 = new Review();
        review1.setId("review1");
        review1.setCafeId(cafeId);
        review1.setUserId("user1");
        review1.setUsername("testuser1");
        review1.setOverallRating(5);
        review1.setText("Great coffee!");
        review1.setStatus("APPROVED");
        review1.setCreatedAt(java.time.Instant.now());

        Review review2 = new Review();
        review2.setId("review2");
        review2.setCafeId(cafeId);
        review2.setUserId("user2");
        review2.setUsername("testuser2");
        review2.setOverallRating(4);
        review2.setText("Nice ambiance");
        review2.setStatus("APPROVED");
        review2.setCreatedAt(java.time.Instant.now().minusSeconds(3600));

        return Arrays.asList(review1, review2);
    }
}

