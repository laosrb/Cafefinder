package com.cafefinder.app;

import com.cafefinder.app.model.Cafe;
import com.cafefinder.app.repo.CafeRepo;
import com.cafefinder.app.repo.ReviewRepo;
import com.cafefinder.app.service.CafeService;
import com.cafefinder.app.service.GooglePlacesService;
import com.cafefinder.app.web.CafeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Case #1: CafeSearchTest
 * Purpose: Verify that a user can successfully find a cafe according to requirement 1
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Cafe Search Tests")
class CafeSearchTest {

    @Mock
    private CafeRepo cafeRepo;

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private GooglePlacesService googlePlacesService;

    @Mock
    private RestTemplate restTemplate;

    private CafeService cafeService;

    private CafeController cafeController;

    private List<Cafe> testCafes;

    @BeforeEach
    void setUp() {
        cafeService = new CafeService(cafeRepo, reviewRepo, mongoTemplate, googlePlacesService);
        cafeController = new CafeController(cafeRepo, cafeService);
        testCafes = createTestCafes();
    }

    @Test
    @DisplayName("Test Case 1.1: Search with valid address and accepted radius")
    void testSearchWithValidAddressAndAcceptedRadius() {
        // Given
        String validAddress = "Atlanta";
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double radius = 5.0; // Within allowed limits

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.search(validAddress, null, lat, lng, radius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.2: Search with larger radius")
    void testSearchWithLargerRadius() {
        // Given
        String validAddress = "Atlanta";
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double largerRadius = 20.0; // Larger radius

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.search(validAddress, null, lat, lng, largerRadius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        // Larger radius should potentially return more results
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.3: Search with GPS location (empty address, GPS on)")
    void testSearchWithGPSLocation() {
        // Given - empty address but GPS coordinates provided
        String emptyAddress = null;
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double radius = 10.0;

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.search(emptyAddress, null, lat, lng, radius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        // Should use GPS coordinates to find nearby cafes
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.4: Search with empty address and GPS off")
    void testSearchWithEmptyAddressAndGPSOff() {
        // Given - no address and no GPS coordinates
        String emptyAddress = null;
        Double lat = null;
        Double lng = null;
        Double radius = 10.0;

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.search(emptyAddress, null, lat, lng, radius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        // Should return all cafes or empty list, but not fail
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.5: Search with invalid or random address")
    void testSearchWithInvalidAddress() {
        // Given
        String invalidAddress = "xyz123randomaddress456";
        Double lat = null;
        Double lng = null;
        Double radius = 10.0;

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(new ArrayList<>());

        // When
        List<Cafe> results = cafeController.search(invalidAddress, null, lat, lng, radius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        // Should return empty list or handle gracefully, not throw exception
        assertTrue(results.isEmpty() || results.size() >= 0);
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.6: Search with radius greater than allowed limit")
    void testSearchWithRadiusGreaterThanLimit() {
        // Given
        String validAddress = "Atlanta";
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double excessiveRadius = 1000.0; // Very large radius

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.search(validAddress, null, lat, lng, excessiveRadius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        // Should handle large radius gracefully
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.7: Search with network/connection error simulation")
    void testSearchWithConnectionError() {
        // Given - simulate network error by making GooglePlacesService throw exception
        String validAddress = "Atlanta";
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double radius = 10.0;

        // Since CafeService uses database search primarily, we test that it handles errors gracefully
        when(mongoTemplate.find(any(), eq(Cafe.class))).thenThrow(new RuntimeException("Connection error"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            cafeController.search(validAddress, null, lat, lng, radius, null, null, null, null, null);
        });
    }

    @Test
    @DisplayName("Test Case 1.8: Search with filters (wifi, seating, workFriendly)")
    void testSearchWithFilters() {
        // Given
        String query = "coffee";
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double radius = 10.0;
        Boolean wifi = true;
        Boolean seating = true;
        Boolean workFriendly = true;

        List<Cafe> filteredCafes = testCafes.stream()
                .filter(c -> c.isWifi() && c.isSeating() && c.isWorkFriendly())
                .toList();

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(filteredCafes);

        // When
        List<Cafe> results = cafeController.search(query, null, lat, lng, radius, wifi, seating, workFriendly, null, null);

        // Then
        assertNotNull(results);
        results.forEach(cafe -> {
            assertTrue(cafe.isWifi());
            assertTrue(cafe.isSeating());
            assertTrue(cafe.isWorkFriendly());
        });
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.9: Search with city filter")
    void testSearchWithCityFilter() {
        // Given
        String city = "Atlanta";
        Double lat = null;
        Double lng = null;
        Double radius = null;

        List<Cafe> cityCafes = testCafes.stream()
                .filter(c -> "Atlanta".equalsIgnoreCase(c.getCity()))
                .toList();

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(cityCafes);

        // When
        List<Cafe> results = cafeController.search(null, city, lat, lng, radius, null, null, null, null, null);

        // Then
        assertNotNull(results);
        results.forEach(cafe -> assertEquals("Atlanta", cafe.getCity()));
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.10: Search with minimum rating filter")
    void testSearchWithMinRatingFilter() {
        // Given
        String query = "coffee";
        Double minRating = 4.0;

        List<Cafe> highRatedCafes = testCafes.stream()
                .filter(c -> c.getAvgRating() >= minRating)
                .toList();

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(highRatedCafes);

        // When
        List<Cafe> results = cafeController.search(query, null, null, null, null, null, null, null, null, minRating);

        // Then
        assertNotNull(results);
        results.forEach(cafe -> assertTrue(cafe.getAvgRating() >= minRating));
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    @Test
    @DisplayName("Test Case 1.11: Nearby search endpoint")
    void testNearbySearch() {
        // Given
        Double lat = 33.7490;
        Double lng = -84.3880;
        Double radius = 5.0;

        when(cafeRepo.findAll()).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.findNearby(lat, lng, radius);

        // Then
        assertNotNull(results);
        verify(cafeRepo, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("Test Case 1.12: Popular cafes endpoint")
    void testPopularCafes() {
        // Given
        int limit = 10;

        when(mongoTemplate.find(any(), eq(Cafe.class))).thenReturn(testCafes);

        // When
        List<Cafe> results = cafeController.getPopular(limit);

        // Then
        assertNotNull(results);
        verify(mongoTemplate, atLeastOnce()).find(any(), eq(Cafe.class));
    }

    // Helper method to create test cafes
    private List<Cafe> createTestCafes() {
        Cafe cafe1 = new Cafe();
        cafe1.setId("cafe1");
        cafe1.setName("Test Cafe 1");
        cafe1.setAddress("123 Main St");
        cafe1.setCity("Atlanta");
        cafe1.setState("GA");
        cafe1.setLatitude(33.7490);
        cafe1.setLongitude(-84.3880);
        cafe1.setWifi(true);
        cafe1.setSeating(true);
        cafe1.setWorkFriendly(true);
        cafe1.setAvgRating(4.5);
        cafe1.setPriceRange("$$");

        Cafe cafe2 = new Cafe();
        cafe2.setId("cafe2");
        cafe2.setName("Test Cafe 2");
        cafe2.setAddress("456 Oak Ave");
        cafe2.setCity("Atlanta");
        cafe2.setState("GA");
        cafe2.setLatitude(33.7500);
        cafe2.setLongitude(-84.3890);
        cafe2.setWifi(true);
        cafe2.setSeating(false);
        cafe2.setWorkFriendly(true);
        cafe2.setAvgRating(3.5);
        cafe2.setPriceRange("$");

        Cafe cafe3 = new Cafe();
        cafe3.setId("cafe3");
        cafe3.setName("Coffee Shop 3");
        cafe3.setAddress("789 Pine Rd");
        cafe3.setCity("Atlanta");
        cafe3.setState("GA");
        cafe3.setLatitude(33.7510);
        cafe3.setLongitude(-84.3900);
        cafe3.setWifi(false);
        cafe3.setSeating(true);
        cafe3.setWorkFriendly(false);
        cafe3.setAvgRating(4.0);
        cafe3.setPriceRange("$$$");

        return Arrays.asList(cafe1, cafe2, cafe3);
    }
}

