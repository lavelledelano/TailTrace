# TailTrace: Tracking Every Pawprint 🐾

A community Android application for reporting and finding lost pets, developed as part of the OPSC6312 Part 2 group project.

## Team Members

- **Lavelle Dalman** | ST10290032
- **Eden Gwenda** | ST10452860

## Purpose

TailTrace is a community application designed to help pet owners and community members report, locate and share information about missing pets.

Pet owners can create missing-pet cases by providing information such as photographs, breed, colour, description and last-seen location. Community members can browse nearby missing-pet cases, view case details and submit sighting reports to assist with locating missing pets.

## Video Demonstration Video Link:
https://youtube.com/shorts/Iql9AnZHzLQ?si=FIp0DI4iWbnE1V6l 

## Features

### 1. Report a Missing Pet

Users can report a missing pet by providing:

- Photo
- Breed
- Colour
- Description
- Last-seen location

### 2. Browse and View Missing-Pet Cases

Users can browse active missing-pet cases using a searchable and filterable list. Users can view full case details, use the call-owner function and manage the status of cases where applicable.

Available case statuses include:

- Active
- Paused
- Reunited
- Closed

### 3. Report a Sighting

Community members can submit a sighting report containing:

- Photo
- Location
- Direction
- Notes
- Safety reminder

### 4. Sign In and Register

Users can create an account and sign in to the application. Passwords are hashed using bcrypt on the server, while authenticated sessions use a JWT token.

### 5. Settings

Users can manage application settings, including:

- Edit profile
- Dark mode
- Alerts preference
- Search radius
- Log out

## Design Considerations

The design of TailTrace focuses on providing a simple and user-friendly way for community members to report and locate missing pets.

The application provides separate functionality for reporting missing pets, browsing existing cases and submitting sightings. Information such as photographs, descriptions and locations is included to help users identify missing pets.

Input and authentication functionality are handled through the Android application and REST API. The application also provides settings that allow users to manage their profile and preferences.

Security was considered during the design by ensuring that passwords are not stored in plain text and that authenticated sessions use JWT tokens. The backend uses bcrypt password hashing to protect stored passwords.

The application also uses a REST API connected to MongoDB Atlas so that information can be stored and accessed between the Android application and backend.

## Technology Stack

### Android Application

- Kotlin
- Jetpack Compose
- Retrofit
- DataStore
- Coil

### Backend

- Node.js
- Express.js
- MongoDB Atlas
- Railway
- JWT authentication
- bcrypt password hashing

## REST API

The Android application communicates with the backend through a REST API.

**Base URL:** https://tailtrace-production.up.railway.app/

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Sign in and return JWT |
| PUT | `/api/users/me` | Update profile |
| POST | `/api/pets` | Create missing-pet case |
| GET | `/api/pets/nearby` | Retrieve active cases within a radius |
| GET | `/api/pets/:id` | Retrieve case details |
| PATCH | `/api/pets/:id/status` | Allow owner to update case status |
| POST | `/api/sightings` | Submit a sighting |
| GET | `/api/pets/:id/sightings` | Retrieve sightings for a case |

## Database

TailTrace uses **MongoDB Atlas** as the backend database. The REST API communicates with the database to store and retrieve application information, including user accounts, missing-pet cases and sighting reports.

The backend is deployed using **Railway**.

## Security

Security was considered as part of the authentication implementation.

- Passwords are never stored in plain text.
- The server stores passwords using bcrypt hashing.
- The Android application stores a session token rather than the user's password.
- JWT authentication is used to maintain authenticated sessions.

## Run the App

1. Clone the repository and open the root folder in Android Studio.
2. Set `BASE_URL` in `Api.kt` to the live API address shown above.
3. Run the application on an Android emulator or physical Android device.

The application supports **Android 8.0 / API 26 and higher**.

## Run the API Locally

The backend API is located in the `api` folder.

```bash
cd api
npm install
npm start
