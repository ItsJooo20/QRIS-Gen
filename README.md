# QRIS Dynamic Generator

Android app to generate dynamic QRIS codes from static QRIS. Built with Kotlin and MVVM architecture as a learning project.

---

## Features

- Parse static QRIS payload(now u can upload it from storage or direct scan)
- Extract merchant information
- Generate dynamic QRIS with custom amounts
- Transaction history stored locally
- Offline, no internet required

---

## Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM
- **Database:** Room
- **UI:** Material Design 3
- **Async:** Coroutines + LiveData
- **QR Code:** ZXing

---

## Getting Started

### Prerequisites

- Android Studio Meerkat 2024.3.2(What I Use)
- Min SDK: 24 (Android 7.0)
- Target SDK: 36 (Android 15)


---

### Installation

```bash
git clone https://github.com/ItsJooo20/QRIS-Gen.git
cd QRIS-Gen
```

Open in Android Studio and run! or u can download:

- [Download APK here](app-debug.apk)

---

## How It Works

1. **Paste/upload static QRIS** → App extracts merchant info
2. **Enter amount** → App generates dynamic QRIS
3. **Show QR code** → Customer scans and pays
4. **Save to history** → Track all transactions

---

## Screenshots

<p align="center">
  <img src="screenshots/main.png" width="200"/>
  <img src="screenshots/dialog.png" width="200"/>
  <img src="screenshots/history.png" width="200"/>
  <img src="screenshots/scan.png" width="200"/>
  <img src="screenshots/setup.png" width="200"/>
</p>

---
