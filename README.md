# 🏥 Project Health GZ  
*Transforming Healthcare with Secure, Scalable Innovation*

![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=flat-square&logo=kotlin)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg?style=flat-square&logo=firebase)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A.svg?style=flat-square&logo=gradle)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat-square&logo=android)

---

## 📌 Overview
**Project Health GZ** is an all-in-one, open-source healthcare platform that streamlines patient management, medical records, appointment scheduling, and real-time analytics.  
Built with modern Android technologies, it delivers **security**, **scalability**, and **simplicity** in every interaction.

---

## ✨ Key Features
| Feature | Description |
|---------|-------------|
| 🔐 **Security & Role-Based Access** | Fine-grained Firestore security rules protect sensitive data; role-specific permissions keep workflows compliant. |
| 📱 **Modern Android App** | 100 % Kotlin + Jetpack Compose + Firebase for a responsive, feature-rich user experience. |
| 🧩 **Modular Gradle Build** | Reproducible builds across modules and environments via Gradle Convention Plugins. |
| ⚡ **Real-Time Data & Admin Controls** | Live updates + administrative dashboards for efficient clinic operations. |
| 📄 **PDF Utilities** | Generate, share, and preview medical reports without leaving the app. |
| 🔍 **Fast Autocomplete** | Binary-search powered suggestions cut input time and reduce errors. |

---

## 🚀 Getting Started

### 1. Prerequisites
- Android Studio **Giraffe** or newer  
- JDK 17+  
- Firebase project with **Firestore**, **Authentication**, and **Storage** enabled  
- (Optional) Google Cloud service account for CI/CD

### 2. Installation
```bash
# Clone the repository
git clone https://github.com/your-org/project-health-gz.git
cd project-health-gz

# Install git-hooks
./gradlew installGitHooks

# Copy your google-services.json to app/
cp ~/Downloads/google-services.json app/

# Build
./gradlew assembleDebug
```

### 3. Run
```bash
./gradlew installDebug           # Install on connected device
./gradlew run                    # Desktop preview (Jetpack Compose Desktop)
```

---

## 🧪 Testing
| Command | Purpose |
|---------|---------|
| `./gradlew test` | Unit tests |
| `./gradlew connectedAndroidTest` | Android instrumented tests |
| `./gradlew koverHtmlReport` | Coverage report in `build/reports/kover/html/index.html` |

---

## 📂 Project Structure
```
project-health-gz/
├── app/                     # Android application
├── core/                    # Shared domain & data models
├── data/                    # Repositories & data sources
├── feature-patients/        # Patient management feature module
├── feature-schedule/        # Appointment scheduling feature module
└── build-logic/             # Convention plugins & build utilities
```

---

## 🔧 Tech Stack
- **Language**: Kotlin 1.9 + Coroutines + Flow  
- **UI**: Jetpack Compose + Material 3  
- **DI**: Hilt  
- **Database**: Cloud Firestore (offline cache)  
- **Auth**: Firebase Auth (email, Google, phone)  
- **Storage**: Firebase Storage + Cloud Functions for PDF generation  
- **CI/CD**: GitHub Actions → Firebase App Distribution / Google Play  
- **Code Quality**: ktlint + Detekt + Kover  

---

## 🤝 Contributing
We welcome contributions!  
Please read [CONTRIBUTING.md](CONTRIBUTING.md) and open an issue before submitting PRs.


