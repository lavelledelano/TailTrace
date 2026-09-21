# TailTrace: Tracking Every Pawprint 🐾

A community app for reporting and finding lost pets.

Team Members (2): Lavelle Dalman (ST10290032), Eden Gwenda (ST10452860)

## Purpose
Owners post missing pets with photos and last-seen location. Community members browse
nearby cases, view case details and submit sighting reports to help reunite pets with
their owners, everyone wins.

## Features
1. Report a missing pet — photo, breed, colour, description and last-seen location.
2. Browse and view cases — searchable, filterable list of active cases with full details, plus a call-owner button and owner status controls (Active / Paused / Reunited / Closed).
3. Report a sighting — photo, location, direction, notes and a safety reminder.
4. Sign in / register — passwords hashed with bcrypt on the server; session kept with a JWT token.
5. Settings — edit profile (synced to the server), dark mode, alerts preference, search radius, log out.

## Tech stack
Kotlin, Jetpack Compose, Retrofit, DataStore, Coil · Node.js, Express, MongoDB Atlas, Railway.

## REST API
Base URL:https://tailtrace-production.up.railway.app/

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Create account |
| POST | /api/auth/login | Sign in, returns JWT |
| PUT | /api/users/me | Update profile |
| POST | /api/pets | Create missing-pet case |
| GET | /api/pets/nearby | Active cases within a radius |
| GET | /api/pets/:id | Case details |
| PATCH | /api/pets/:id/status | Owner updates status |
| POST | /api/sightings | Submit a sighting |
| GET | /api/pets/:id/sightings | Sightings for a case |

## Security
Passwords are never stored in plain text. The server stores only a bcrypt hash; the
app stores only a session token, never the password.

## Run the app
1. Clone the repo and open the root folder in Android Studio.
2. Set BASE_URL in Api.kt to the live API address above.
3. Run on an emulator or a phone (Android 8.0 / API 26+).

## Run the API locally
    cd api
    npm install
    # create .env with MONGODB_URI and JWT_SECRET
    npm start

## Tests
- API tests: cd api && npm test
- Android tests: ./gradlew testDebugUnitTest
- Both run automatically on every push via GitHub Actions (see the Actions tab).

## Known limitations / future work
Push notifications (Firebase Cloud Messaging), a live map view, multilingual support,
offline caching.
