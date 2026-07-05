# 🛡️ ExtSecure

ExtSecure is an Android application that analyzes Google Chrome extensions and predicts whether they are potentially malicious using a Machine Learning model. The app provides users with a simple way to evaluate browser extensions by analyzing their permissions and metadata before installation.

## Features

- 🔍 Analyze Chrome extensions using their Extension ID
- 🤖 Machine Learning based risk prediction
- 📊 Risk score and risk level classification
- 📄 Displays extension information
  - Name
  - Description
  - Version
  - Permissions
- 📚 Scan history
- 🌐 FastAPI backend deployed on Render
- 📱 Clean Android interface built with Kotlin

---

## Tech Stack

### Android
- Kotlin
- Android Studio
- Retrofit
- Material Design

### Backend
- Python
- FastAPI
- XGBoost
- Uvicorn

### Deployment
- Render

---

## Architecture

```
                +----------------+
                | Android App    |
                |   (Kotlin)     |
                +-------+--------+
                        |
                    Retrofit
                        |
                        v
              +------------------+
              | FastAPI Backend  |
              +--------+---------+
                       |
        Downloads Chrome Extension (.crx)
                       |
                       v
           Extract Manifest & Features
                       |
                       v
             XGBoost ML Model
                       |
                       v
      Risk Score + Risk Level + Metadata
                       |
                       v
               Returned to Android App
```

---

## How It Works

1. User enters a Chrome Extension ID.
2. The Android app sends the request to the FastAPI server.
3. The backend downloads the extension package.
4. The extension is extracted and analyzed.
5. Features are generated from the manifest.
6. The trained XGBoost model predicts the risk.
7. Results are displayed inside the app.

---

## Project Structure

```
ExtSecure/
│
├── app/                  # Android application
│
├── backend/
│   ├── main.py
│   ├── feature_extractor.py
│   ├── xgb_extension_model.pkl
│   ├── feature_columns.pkl
│   └── requirements.txt
│
└── README.md
```

---

## API Response

```json
{
  "extension_id": "...",
  "name": "...",
  "description": "...",
  "version": "...",
  "permissions": [],
  "risk_score": 0.87,
  "risk_level": "High"
}
```

---

## Future Improvements

- Dynamic permission analysis
- Static + behavioral analysis
- VirusTotal integration
- Explainable AI (feature importance)
- Offline model inference
- Chrome Web Store search

---

## Installation

### Clone Repository

```bash
git clone https://github.com/Monish-1126/ExtSecure.git
```

### Android

- Open in Android Studio
- Sync Gradle
- Run the application

### Backend
Already hosted on render.

---

## Screenshots

 Home Screen
 
  <img width="244" height="420" alt="image" src="https://github.com/user-attachments/assets/bdd5a08a-3416-4036-b8d1-201096fbeedd" />

Analysis Result

<img width="244" height="420" alt="image" src="https://github.com/user-attachments/assets/938d208e-c0ff-4c67-ba70-9d328caac1ea" />

 History
 
 <img width="244" height="420" alt="image" src="https://github.com/user-attachments/assets/fcccde55-3e0f-4120-86a1-751f6a731662" />
 
 Settings Screen
 
 <img width="244" height="420" alt="image" src="https://github.com/user-attachments/assets/162e0700-b9ca-4eac-b3b4-4f5980cca872" />

---

## Author

**Monish**

GitHub: https://github.com/Monish-1126

**B S Bharath Kumar Reddy**

GitHub: https://github.com/bkreddy2006

**Maridi Kare Sree Sai Dheeraj**

Github: https://github.com/dheerrraj99

**Pappala Sai Sumanth**

Github: https://github.com/sumanth-751

---

If you found this project useful, consider giving it a ⭐.
