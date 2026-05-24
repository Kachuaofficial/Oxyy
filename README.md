# Oxy - Student Attendance Tracker

Oxy is an Android mini project built as a 4th semester AIML student project. The app helps students track daily attendance, check attendance percentage, plan leaves, and keep their academic profile synced using Firebase.

## Project Overview

The main idea of Oxy is to make attendance tracking simple for students. Instead of calculating attendance manually, a student can mark each day as present, absent, or holiday. The app then calculates the attendance percentage, streak, safe leave count, and required present days to reach the target attendance.

This project uses Jetpack Compose for UI and Firebase for authentication and cloud database storage.

## Features

- Google Sign-In using Firebase Authentication
- New user profile setup after login
- Semester and department selection using chips
- Attendance tracking start date selection
- Mark attendance from calendar
- Present, absent, and holiday status support
- Future dates cannot be marked
- Sundays are ignored by default unless manually marked
- Attendance percentage calculation
- Present, absent, and tracked day summary
- Attendance streak calculation
- Leave planner for checking future attendance impact
- Attendance history screen
- Google profile image display
- Firestore database integration
- Dark mode support with Material 3 theme

## Tech Stack

- Kotlin
- Android
- Jetpack Compose
- Material 3
- Firebase Authentication
- Cloud Firestore
- Google Sign-In / Credential Manager
- Coil for profile image loading
- Gradle Kotlin DSL

## Firebase Collections

The app stores user and attendance data in Firestore.

### User Document

Path:

```text
users/{uid}
```

Fields:

```text
uid
email
displayName
photoUrl
semester
department
rollNumber
attendanceStartDate
profileComplete
role
createdAt
lastLoginAt
updatedAt
```

### Attendance Records

Path:

```text
users/{uid}/attendance/{yyyy-MM-dd}
```

Fields:

```text
date
status
reason
updatedAt
```

Status values:

```text
PRESENT
ABSENT
HOLIDAY
NOT_MARKED
```

## Screens

- Auth Screen: Google login
- Profile Setup Screen: academic profile setup
- Home Screen: attendance summary and leave planner
- Attendance Screen: calendar-based attendance marking
- History Screen: list of all attendance records
- Profile Screen: student details and sign out

## How Attendance Is Calculated

Only `PRESENT` and `ABSENT` records are counted in attendance percentage.

```text
Attendance % = Present Days / (Present Days + Absent Days) * 100
```

Holidays and unmarked days are not counted.

Sundays are ignored by default unless the student manually marks that Sunday.

## Setup Instructions

1. Clone or open the project in Android Studio.
2. Create a Firebase project.
3. Add an Android app in Firebase with package name:

```text
com.invatech.oxy
```

4. Download `google-services.json`.
5. Place it inside:

```text
app/google-services.json
```

6. Enable Google Sign-In in Firebase Authentication.
7. Add SHA-1/SHA-256 fingerprints in Firebase project settings.
8. Create a Firestore database.
9. Sync Gradle and run the app.

## Build Command

```bash
./gradlew :app:compileDebugKotlin
```

On Windows:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

## Project Structure

```text
app/src/main/java/com/invatech/oxy
├── auth
│   ├── AuthRepository.kt
│   └── AuthViewModel.kt
├── data
│   ├── AttendanceAnalytics.kt
│   ├── AttendanceRepository.kt
│   └── AttendanceViewModel.kt
├── navigation
│   └── NavDestination.kt
├── screens
│   ├── AuthScreen.kt
│   ├── AttendanceHistoryScreen.kt
│   ├── AttendanceScreen.kt
│   ├── HomeScreen.kt
│   ├── MainScreen.kt
│   ├── ProfileScreen.kt
│   ├── ProfileSetupScreen.kt
│   └── UserProfileImage.kt
└── ui/theme
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## Learning Outcomes

Through this mini project, I learned:

- How to build Android UI using Jetpack Compose
- How to use Firebase Authentication with Google Sign-In
- How to store and read user data from Firestore
- How to manage app state using ViewModel and StateFlow
- How to design a simple attendance calculation system
- How to build a dark-mode friendly Material 3 app
- How to organize a medium-sized Android project

## Future Scope

- Subject-wise attendance tracking
- Timetable-based automatic attendance suggestions
- Monthly attendance report export
- Push notifications for low attendance
- Edit profile details
- Teacher/admin portal
- Charts for monthly attendance trends

## Author

Made as a 4th semester AIML mini project.

```text
Project Name: Oxy
Domain: Android App Development
Category: Student Utility App
```
