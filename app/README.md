# SmartTimeline 🕒📓

SmartTimeline is an Android application designed for personal journaling and timeline management.  
It allows users to record daily posts, analyze emotional and activity patterns, and generate AI-powered summaries — while keeping data stored locally for privacy.

---

## ✨ Features

- 📝 **Timeline-based Journaling**
    - Create, edit, and delete posts
    - Each post supports text, mood, location, tags, and images

- 🔍 **Search & Filtering**
    - Search posts by keywords
    - Filter timeline by mood

- 📊 **Analytics Dashboard**
    - Mood distribution visualization
    - Posts per day statistics
    - Tag usage analysis
    - Implemented using interactive charts

- 🤖 **AI-Powered Summaries**
    - Weekly, monthly, and yearly summaries
    - Powered by the **Groq API**

- 🔔 **Daily Notifications**
    - Reminder notifications to encourage journaling

- 📤 **Export / Import**
    - Backup and restore journal data using JSON

- 🔐 **Permissions Handling**
    - Camera, storage, and location permissions handled safely

---

## 🏗️ Architecture

SmartTimeline follows the **MVVM (Model-View-ViewModel)** architecture:

UI (Fragments)
↓
ViewModel (LiveData)
↓
Repository
↓
Room Database

This architecture ensures:
- Clear separation of concerns
- Lifecycle-aware data handling
- Improved maintainability and scalability

---

## 📁 Project Structure

app/
└── src/main/java/com/example/smarttimeline/
├── ai/ # AI logic and API integration
├── data/ # Room database and repositories
├── notification/ # Notification handling
├── ui/ # Fragments and adapters
├── util/ # Utility classes
├── viewmodel/ # ViewModels (MVVM)
└── src/main/res/
├── layout/ # XML layouts
├── drawable/ # Icons and images
└── values/ # Colors, themes, styles

---

## 🛠️ Technologies Used

| Technology | Description |
|----------|-------------|
| Java | Core application logic |
| XML | UI layouts |
| Android Studio | Development IDE |
| Room ORM | Local database |
| LiveData & ViewModel | Lifecycle-aware components |
| WorkManager | Background AI tasks |
| MPAndroidChart | Data visualization |
| Gson | JSON serialization |
| HttpURLConnection | API networking |
| Gradle | Build system |

---

## ⚙️ Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/SmartTimeline.git
Open the project in Android Studio

Sync Gradle dependencies

Add your Groq API key from the Settings screen

Run the app on an emulator or physical device

## 🔑 API Configuration

AI summaries require a valid Groq API key

The API key is stored locally and can be updated from the Settings screen

## 🚀 Future Enhancements

Offline AI summaries

Cloud synchronization

Multi-device support

Advanced UI animations

Password or biometric app lock

## 📜 License

This project is intended for academic and learning purposes.
You are free to fork and modify it for personal or educational use.

## 🙌 Acknowledgements

Android Jetpack libraries

MPAndroidChart

Groq AI API


---
