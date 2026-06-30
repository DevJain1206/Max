# Max (MaxChat) 🤖

Max is an Android chatbot application built with Kotlin and Jetpack Compose. In the future, this application will evolve to work like a personal assistant (similar to Jarvis), integrating an advanced voice pipeline and wake‑word detection.

Currently, Max functions as a sleek, interactive chatbot interface.

---

## 🚀 Features

- **Chatbot Interface**: A clean, modern UI for chatting with LLM backend.
- **Jetpack Compose**: Built using Android's modern toolkit for native UI.
- **Future Jarvis-like capabilities**: Plan for offline/online wake‑word detection, automated speech recognition (ASR), and natural text‑to‑speech (TTS).

---

## 🛠️ Upcoming Roadmap (Jarvis-like Voice Mode)

As outlined in [TODO.txt](TODO.txt):
1. **Wake‑word Detection**: Evaluate and integrate Mycroft Precise, Vosk‑tiny, OpenWakeWord, or PocketSphinx.
2. **Speech‑to‑Text (ASR)**: Whisper (OpenAI) or Google Speech‑to‑Text integration.
3. **Text‑to‑Speech (TTS)**: ElevenLabs, Google Cloud, or Azure TTS.
4. **State Management**: Lightweight state manager for handling:
   `idle` ➡️ `listening` ➡️ `processing` ➡️ `speaking`

---

## 📦 Requirements & Setup

- **Android Studio** (Koala or newer recommended)
- **JDK 17+**
- **Android SDK 34+**

1. Clone this repository:
   ```bash
   git clone https://github.com/DevJain1206/Max.git
   ```
2. Open the project in Android Studio.
3. Let Gradle sync and build the project.
4. Run on an emulator or physical device.

---

## 🔒 License

Proprietary / Private project.
