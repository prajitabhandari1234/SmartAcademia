# SmartAcademia

An Android app that helps students manage course units, tasks, and study schedules with automatic schedule generation, reminders, and an analytics dashboard to track progress.

---

## Overview

SmartAcademia is a native Android application built in Kotlin. Students register and log in (with OTP verification), add their course units, and create tasks under each unit. The app automatically generates study schedules from task deadlines, sends reminder notifications, and visualizes workload and progress through an analytics dashboard.

---

## Features

- **User authentication** - registration, login, and OTP verification
- **Course unit management** - add and track units
- **Task management** - create, view, and organize tasks per unit
- **Automatic schedule generation** - builds a study schedule from task deadlines
- **Reminders & notifications** - scheduled alerts so tasks aren't missed
- **Analytics dashboard** - visual charts of task completion and workload
- **Maps integration** - location-based features via Google Maps
- **Local persistence** - offline-first data storage with Room

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM (ViewModel + LiveData) |
| Local Database | Room |
| Networking | Retrofit + Gson |
| Async | Kotlin Coroutines |
| UI | Material Components, View Binding, Navigation Component |
| Charts | MPAndroidChart |
| Location | Google Maps SDK, Play Services Location |
| Testing | JUnit, Espresso |

---

## Project Structure

```
app/src/main/java/au/edu/cqu/smartacademia/
├── activities/       # Login, Register, OTP, Maps, Add/Regenerate Schedule, Unit Detail
├── fragments/        # Dashboard, Task List, Schedule, Analytics, Unit Dashboard
├── adapter/          # RecyclerView adapters (Task, Unit)
├── viewmodel/        # TaskViewModel, UnitViewModel, UserViewModel
├── database/         # Room entities, DAOs, and repositories (User, Task, CourseUnit)
├── network/          # Retrofit API service and client setup
├── model/            # API response models
└── utils/            # Schedule generation, reminders, notifications, OTP generation
```

---

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 11
- Android SDK 36 (min SDK 23)

### Setup
1. Clone the repository
   ```
   git clone https://github.com/prajitabhandari1234/SmartAcademia.git
   ```
2. Open the project in Android Studio
3. Add your own Google Maps API key to `secrets.properties` (not committed to version control)
4. Sync Gradle and run on an emulator or device

---

## Known Limitations

- Location-based features require Google Play Services
- OTP verification is currently implemented for demonstration purposes and not integrated with a production SMS/email provider

---

## Author

**Prajita Bhandari**
