# Car Rental System

Full stack car rental web app.

## Tech
- Backend: Spring Boot, Spring Security, JWT, JPA, MySQL
- Frontend: React, Vite, Tailwind CSS, Axios

## Features
- Browse cars and check availability by dates
- Register and login with JWT
- Book a car, cancel a booking
- Overlapping bookings are blocked
- Admin dashboard to manage cars and bookings

## Run locally
Backend (needs Java 21 and MySQL):
1. Set env variables: DB_PASSWORD, JWT_SECRET
2. Run CarrentalApplication

Frontend:
1. cd frontend
2. npm install
3. npm run dev