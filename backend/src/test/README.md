# JUnit 5 Test Suite for CafeFinder Backend

This directory contains a comprehensive JUnit 5 testing suite for the CafeFinder backend application.

## Test Structure

The test suite is organized into four main test classes, each corresponding to a specific test case:

### 1. CafeSearchTest
**Purpose**: Verify that a user can successfully find a cafe according to requirement 1.

**Test Coverage**:
- Search with valid address and accepted radius
- Search with larger radius
- Search with GPS location (empty address, GPS on)
- Search with empty address and GPS off
- Search with invalid or random address
- Search with radius greater than allowed limit
- Search with network/connection error simulation
- Search with filters (wifi, seating, workFriendly)
- Search with city filter
- Search with minimum rating filter
- Nearby search endpoint
- Popular cafes endpoint

### 2. CafeOverviewTest
**Purpose**: Verify that a user can successfully view a cafe overview according to requirement 2.

**Test Coverage**:
- View cafe with complete profile (hours, menu, reviews)
- View cafe with partial profile (missing hours)
- View cafe with missing menu
- View cafe with no reviews
- View nonexistent cafe (should return 404)
- View cafe menu endpoint
- View cafe with missing location
- Select different cafe from search results
- View cafe with missing reviews but has other data
- Verify cafe overview shows all required information

### 3. AdminReviewApprovalTest
**Purpose**: Verify that an admin can review, approve, or reject user-submitted cafe reviews before they appear publicly.

**Test Coverage**:
- Admin can view pending reviews
- Admin can approve a pending review
- Approved review appears on cafe page
- Admin can reject a pending review
- Rejected review does not appear on cafe page
- Regular user cannot access admin review dashboard
- Logged out user cannot access admin review dashboard
- Admin can view all reviews with status filter
- Admin can view all reviews (ALL status)
- Admin can view dashboard stats
- Approving review removes it from pending list
- Rejecting review with flagged content
- Approving review does not create duplicates
- Review not found error handling

### 4. LeaveReviewTest
**Purpose**: Verify that a user can successfully leave a review according to requirement 10.

**Test Coverage**:
- Create review with valid overallRating (1, 5)
- Create review with overallRating < 1
- Create review with overallRating > 5
- Create review with valid coffeeRating (1, 5)
- Create review with coffeeRating < 1, > 5, null
- Create review with valid tasteRating (1, 5)
- Create review with tasteRating < 1, > 5, null
- Create review with valid ambianceRating (1, 5)
- Create review with ambianceRating < 1, > 5, null
- Create review with valid serviceRating (1, 5)
- Create review with serviceRating < 1, > 5, null
- Create review with null text
- Create review with short text
- Create review with very long text
- Create review with valid overallRating and all optional fields null
- Create review with all valid ratings
- Verify review is saved with PENDING status

## Test Configuration

### Test Profile
Tests use the `test` profile defined in `application-test.properties`:
- Uses a separate test database: `cafe_finder_test`
- JWT secret configured for testing
- Google Places API key is empty to avoid external API calls during tests

### Dependencies
All tests use:
- **JUnit 5** (`@Test`, `@DisplayName`, `@BeforeEach`)
- **Mockito** (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`)
- **Spring Boot Test** (where appropriate for integration tests)

## Running the Tests

### Run all tests:
```bash
mvn test
```

### Run a specific test class:
```bash
mvn test -Dtest=CafeSearchTest
mvn test -Dtest=CafeOverviewTest
mvn test -Dtest=AdminReviewApprovalTest
mvn test -Dtest=LeaveReviewTest
```

### Run a specific test method:
```bash
mvn test -Dtest=CafeSearchTest#testSearchWithValidAddressAndAcceptedRadius
```

## Test Data

Each test class includes helper methods to create test data:
- `createTestCafes()` - Creates sample cafe objects
- `createCompleteCafe()` - Creates a cafe with all fields populated
- `createPartialCafe()` - Creates a cafe with missing optional fields
- `createTestReviews()` - Creates sample review objects
- `createPendingReviews()` - Creates reviews with PENDING status
- `createApprovedReviews()` - Creates reviews with APPROVED status
- `createRejectedReviews()` - Creates reviews with REJECTED status

## Notes

1. **Mocking Strategy**: Tests use Mockito to mock repositories and services, avoiding actual database connections during unit tests.

2. **Security**: Some security annotations are temporarily disabled in the AdminController for testing. In production, these should be enabled.

3. **External Dependencies**: Tests mock the GooglePlacesService to avoid making actual API calls during testing.

4. **Test Isolation**: Each test is independent and uses its own mock setup to ensure test isolation.

## Expected Test Results

All tests should pass when:
- Maven dependencies are properly downloaded
- Test database is available (for integration tests)
- No external services are required (all mocked)

## Troubleshooting

If tests fail:
1. Ensure Maven dependencies are downloaded: `mvn clean install`
2. Check that Java 17 is being used
3. Verify test profile is active
4. Check that MongoDB is running (if integration tests are enabled)

