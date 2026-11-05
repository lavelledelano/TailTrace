# TailTrace: Tracking Every Pawprint 🐾

A community app for reporting and finding lost pets. Built for OPSC6312.

Team: Lavelle Dalman (ST10290032), Eden Gwenda (ST10452860)

🎥 Demo video:https://youtu.be/l2bxWR-t36g?si=5SVJ2hyRcm_beJqo
📦 Latest release and APK: https://github.com/lavelledelano/TailTrace/releases
📝 Release notes: [RELEASE_NOTES.md](RELEASE_NOTES.md)

## Purpose
Losing a pet is stressful, and information about lost pets is scattered across social media posts and
paper flyers. TailTrace gives owners, neighbours and the community one place to report missing pets,
see them on a map, and submit structured sightings that help reunite pets with their owners.

## Features
- **Sign in and register:** passwords are hashed with bcrypt on the server and never stored on the phone. Sessions use a JWT token.
- **Report a missing pet:** photo, species, breed, colour, description and last-seen location.
- **Browse and search cases:** searchable, filterable list of active cases within a chosen radius.
- **Live map:** OpenStreetMap-based map with a pin for every active case, and a preview card when you tap a pin.
- **Case details:** photos, description, call-the-owner button, and owner status controls (Active, Paused, Reunited, Closed).
- **Report a sighting:** photo, location, direction of travel, notes, and a safety reminder.
- **Settings:** edit profile (synced to the server), dark mode, alerts preference, search radius, language, log out.
- **Multi-language:** English, Afrikaans and isiZulu, switchable instantly in Settings.
- **Offline mode with sync:** cases are saved on the phone for reading offline, and anything posted offline is queued and uploaded automatically when the phone reconnects.

## Innovative features
| Feature from our design | Status in this version |
|---|---|
| Sightings as evidence, not social posts | Implemented: every sighting has a photo, location, time, direction and notes |
| Safe Sighting Mode | Implemented: a safety reminder on the sighting screen (observe from a distance, do not chase) |
| Live pawprint recovery map | Partly implemented: live map with pins for active cases and tap-to-preview |
| Automatic movement and search-radius updates | Not yet implemented (future work) |
| Neighbourhood relay alerts (push notifications) | Not yet implemented (future work) |

## Tech stack
Kotlin, Jetpack Compose, Retrofit, DataStore, SQLite, Coil, osmdroid ·
Node.js, Express, MongoDB Atlas, Vercel.

## Architecture
The Android app talks to a REST API over HTTPS. The API is hosted on Vercel and stores data in MongoDB Atlas.
Offline data is kept in a local SQLite database on the phone.

**Live API:** https://tail-trace-pied.vercel.app

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
Passwords are never stored in plain text. The server stores only a bcrypt hash, and the app stores only a
session token. All traffic uses HTTPS.

## Run the app
1. Clone the repo and open the root folder in Android Studio.
2. Make sure `BASE_URL` in `Api.kt` is the live API address above.
3. Run on an emulator or a phone (Android 8.0 / API 26 or higher), or install the APK from the Releases page.

## Run the API locally
    cd api
    npm install
    # create a .env file with MONGODB_URI and JWT_SECRET
    npm start

## Tests and CI
- API tests: `cd api` then `npm test`
- Android unit tests: `./gradlew testDebugUnitTest`
- Both run automatically on every push with GitHub Actions (see the Actions tab).

## Known limitations and future work
- Single sign-on and push notifications were not completed in this version.
- Pet photos are not shown offline unless they were cached earlier.
- A pet posted offline appears in the list only after it syncs.
- The free hosting plan can be slow on the first request after a quiet period.
