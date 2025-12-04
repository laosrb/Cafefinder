Cafe Finder Prototype



Hi I'm tim...
This is Cafe Finder; fullstack web application with the utilization of mongodb.
Basically Y3lp but for Cafes.. Maybe something more community focused.
Java 17 + Spring Boot 3, MongoDB, React + Vite.

- Modern React frontend with routing and state management
- Spring Boot backend with MongoDB database
- JWT authentication system
- Comprehensive cafe and review management
- Real-time busy hours tracking
- Advanced search with filters
- Map integration (Google Maps)
- responsive UI design


cd frontend
npm install

## Environment Setup

### Frontend Environment Variables

Create `frontend/.env` file:

```env
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
VITE_API_URL=  # Leave empty for local development (uses proxy)
```

1. Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Enable: Maps JavaScript API, Places API
3. Create `frontend/.env` file with your API key

### Backend Environment Variables

The backend uses `application.properties` for configuration. For production, override with environment variables:

- `MONGODB_URI`: MongoDB connection string
- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins
- `cafefinder.app.jwtSecret`: JWT secret key

**Note:** Never commit `.env` files to Git. They are protected by `.gitignore`.


 Terminal 1 - Database
docker compose up -d

 Terminal 2 - Backend  
cd backend
./mvnw spring-boot:run or mvn spring-boot:run dependent on your device.

 Terminal 3 - Frontend
cd frontend
npm run dev

 If you want, just use MongoDB Compass and localhost it for the first terminal, and have two for testing instead.


Features Overview:

- User registration and login with JWT tokens (hashed pwd for security)
- Role-based access control (USER, OWNER*, ADMIN) (*In Progress)
- User profiles with social features (In Progress)
- Review reputation system (In Progress)

- Advanced search with multiple filters
- Location-based search with GPS (In Progress)
- Map view with interactive markers
- Popular sample cafes and trending locations 
- Comprehensive cafe profiles

- Multi-dimensional ratings (overall, coffee, taste, ambiance, service, value)
- Photo upload support (In Progress)
- Taste notes and detailed feedback 
- Amenity verification by reviewers
- Social features (likes, helpful votes) (In Progress)

- Live busy hours tracking (Soon to use Google Maps/Places API for this with the addition of individuals working on it.
- Community-reported wait times (look above)
- Current cafe status updates (In Progress)
- Historical trend analysis (In Progress)

- Interactive Google Maps integration
- GPS-based nearby search (In progress)
- Radius filtering (In progress)
- Location visualization
- Clickable cafe markers

- Frontend: http://localhost:5173
- Backend: http://localhost:8080

  Demo Accounts:
- Username: `coffeelover` / Password: `password123`
- Username: `atlantalocal` / Password: `password123`
- Username: `admin` / Password: `admin123`
 Architecture:

Frontend (React 18)
- Modern component architecture
- React Router for navigation
- Context API for state management
- Tailwind CSS for styling
- Axios for API communication

Backend (Spring Boot 3)
- REST API with comprehensive endpoints
- JWT authentication
- MongoDB data persistence
- Real-time data processing
- Role-based security

Database (MongoDB)
- Document-based data storage
- Efficient querying and indexing
- Flexible schema for rapid development
- Collections: `cafes`, `reviews`, `users`, `busy_hours`, etc.

actual production considerations:

1. Environment Variables: Set up proper environment variables for production
2. API Keys: Secure your Google Maps API key
3. Database: Use MongoDB Atlas or dedicated MongoDB instance
4. Security: Review and harden security settings
5. Performance: Add caching and CDN for static assets
6. Monitoring: Add logging and monitoring tools
7 Auth is simplified for prototype: public endpoints for search/read; restrict POST endpoints when you add JWT.

Customization:

The application is built with modularity in mind. You can:
- Add new cafe amenities
- Customize the rating system
- Integrate additional map providers
- Add photo upload functionality
- Implement push notifications
- Add social features
- Extend models to add more feature fields (wifi, bathrooms, alternative milks, etc.).
---
