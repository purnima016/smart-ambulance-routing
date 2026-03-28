# 🚑 Smart Ambulance Routing App

An Android application designed to assist in medical emergencies by finding 
the nearest available hospital based on emergency type and blood group 
availability in real time.

## ✨ Features

- 🏥 Find nearest hospitals based on emergency type
- 🩸 Check real-time blood group availability
- 🗺️ Google Maps integration with distance & routing
- 🔐 Firebase Authentication (Email + Biometric login)
- 📋 Emergency history tracking
- ⚙️ User preferences for blood group & emergency type
- 🚗 Driver-side functionality
- 📡 Real-time database using Firebase

## 🛠️ Tech Stack

- **Language:** Java
- **Platform:** Android
- **Database:** Firebase Realtime Database
- **Authentication:** Firebase Auth
- **Maps:** Google Maps SDK + Distance Matrix API
- **Architecture:** Fragment-based Navigation
- **Others:** Retrofit, Room Database, Biometric API

## ⚙️ Setup Instructions

1. Clone the repo
```bash
   git clone https://github.com/purnima016/smart-ambulance-routing.git
```
2. Open in Android Studio
3. Add your `google-services.json` in the `app/` folder
4. Add your Maps API key in `local.properties`:
```
   MAPS_API_KEY=your_api_key_here
```
5. Build and run on an emulator or physical device

## 🔐 API Key Security

The Google Maps API key is restricted to this app's package name and 
SHA-1 fingerprint — safe for public repositories.

## 👩‍💻 Developer

**Purnima** — B.Tech CSE, Puducherry Technological University

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
