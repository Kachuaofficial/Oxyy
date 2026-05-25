# Oxy

**Oxy** is a modern Android attendance tracker built for students. It helps users mark daily attendance, calculate attendance percentage automatically, plan safe leaves, and keep academic profile data synced with Firebase.

The project was created as a 4th semester AIML mini project and is designed around a simple goal: make attendance tracking clear, fast, and stress-free.

## Highlights

- Android app built with Kotlin and Jetpack Compose
- Google Sign-In with Firebase Authentication
- Cloud Firestore storage for profiles and attendance records
- Calendar-based attendance marking
- Present, absent, holiday, and not-marked status support
- Automatic attendance percentage calculation
- Safe leave planner and required present-day calculation
- Attendance streak and quick summary cards
- Attendance history and student profile screen
- Material 3 UI with dark mode support

## Problem Statement

Students often calculate attendance manually, which can lead to mistakes and poor leave planning. It can be difficult to know the exact attendance percentage, how many leaves are safe, or how many present days are required to reach the minimum target.

Oxy solves this by providing a mobile-first attendance tracker that calculates attendance insights automatically and stores records securely in the cloud.

## Project Objective

To develop an Android attendance tracker app that helps students:

- Record daily attendance easily
- View accurate attendance percentage
- Track present, absent, and total counted days
- Plan future leaves without falling below the target
- Store attendance data securely with Firebase

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin |
| Platform | Android |
| Frontend | Jetpack Compose, Material 3 |
| Backend | Firebase Authentication, Cloud Firestore |
| Sign-In | Google Sign-In, Credential Manager |
| State Management | ViewModel, StateFlow |
| Image Loading | Coil |
| Build System | Gradle Kotlin DSL |

## Core Features

### Authentication

- Google Sign-In using Firebase Authentication
- Firebase user document creation after login
- Sign-out support from profile screen

### Profile Setup

- Student name and email loaded from Google account
- Semester selection
- Department selection
- Roll number entry
- Attendance tracking start date selection

### Attendance Tracking

- Calendar-based date selection
- Mark any tracked date as:
  - `PRESENT`
  - `ABSENT`
  - `HOLIDAY`
  - `NOT_MARKED`
- Future dates cannot be marked
- Sundays are ignored by default unless manually marked
- Optional reason field for absence

### Attendance Analytics

Oxy calculates:

- Present days
- Absent days
- Tracked days
- Attendance percentage
- Attendance streak
- Safe absent days
- Required present days to reach the target

```text
Attendance % = Present Days / (Present Days + Absent Days) * 100
```

Only `PRESENT` and `ABSENT` records are counted. Holidays and unmarked days are ignored in the percentage calculation.

### Leave Planner

The leave planner lets students select upcoming leave dates and preview how those leaves will affect their attendance percentage. This helps students make better attendance decisions before taking leave.

## Screens

| Screen | Purpose |
| --- | --- |
| Auth Screen | Google login |
| Profile Setup Screen | Academic profile setup |
| Home Screen | Attendance summary, streak, safe leave count, leave planner |
| Attendance Screen | Calendar-based attendance marking |
| History Screen | List of saved attendance records |
| Profile Screen | Student details and sign out |

## Firebase Data Model

### User Document

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

## Project Structure

```text
app/src/main/java/com/invatech/oxy
|-- auth
|   |-- AuthRepository.kt
|   `-- AuthViewModel.kt
|-- data
|   |-- AttendanceAnalytics.kt
|   |-- AttendanceRepository.kt
|   `-- AttendanceViewModel.kt
|-- navigation
|   `-- NavDestination.kt
|-- screens
|   |-- AuthScreen.kt
|   |-- AttendanceHistoryScreen.kt
|   |-- AttendanceScreen.kt
|   |-- HomeScreen.kt
|   |-- MainScreen.kt
|   |-- ProfileScreen.kt
|   |-- ProfileSetupScreen.kt
|   `-- UserProfileImage.kt
`-- ui/theme
    |-- Color.kt
    |-- Theme.kt
    `-- Type.kt
```

## Setup Instructions

1. Open the project in Android Studio.
2. Create a Firebase project.
3. Add an Android app in Firebase with this package name:

```text
com.invatech.oxy
```

4. Download `google-services.json`.
5. Place it inside:

```text
app/google-services.json
```

6. Enable Google Sign-In in Firebase Authentication.
7. Add SHA-1 and SHA-256 fingerprints in Firebase project settings.
8. Create a Cloud Firestore database.
9. Sync Gradle and run the app.

## Build Commands

Compile debug Kotlin:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Build release Android App Bundle:

```powershell
.\gradlew.bat :app:bundleRelease
```

Generated release bundles are available at:

```text
app/build/outputs/bundle/release/
```

## App Version

```text
versionName: 1.3
versionCode: 4
minSdk: 26
targetSdk: 36
```

## Play Store

Play Store package:

```text
com.invatech.oxy
```

Play Store link:

```text
https://play.google.com/store/apps/details?id=com.invatech.oxy
```

Before uploading a release build:

- Build a signed APK or Android App Bundle.
- Add release SHA-1 and SHA-256 fingerprints in Firebase Console.
- Download the updated `google-services.json`.
- Keep Google Sign-In enabled in Firebase Authentication.
- Add the privacy policy URL in Play Console.
- Use `PRIVACY_POLICY.md` as the privacy policy content.

## Learning Outcomes

Through this project, I learned how to:

- Build Android UI using Jetpack Compose
- Integrate Firebase Authentication with Google Sign-In
- Store and read user data from Cloud Firestore
- Manage app state using ViewModel and StateFlow
- Design attendance calculation logic
- Build a dark-mode friendly Material 3 app
- Organize a medium-sized Android project
- Prepare an app for release publishing

## Future Scope

- Subject-wise attendance tracking
- Timetable-based automatic attendance suggestions
- Monthly attendance report export
- Push notifications for low attendance
- Editable profile details
- Teacher/admin portal
- Charts for monthly attendance trends

## Author

```text
Name: Shivam Kumar Mishra
Project Name: Oxy
Domain: Android App Development
Category: Student Utility App
Academic Context: 4th Semester AIML Mini Project
```
