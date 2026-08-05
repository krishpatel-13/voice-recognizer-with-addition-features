# Java Voice Assistant

A lightweight, offline Java desktop application that uses pre-trained AI models (Vosk) for real-time speech-to-text recognition. Speak commands into your microphone to launch desktop apps, trigger system shortcuts, and control scrolling—no internet connection required.

## Features
- **Offline Speech-to-Text:** Built with the Vosk API to process audio locally without cloud dependencies.
- **Application Launcher:** Open browsers (Chrome, Edge) and gaming clients (Steam, Epic Games) with voice commands.
- **System Controls:** Hands-free mouse scrolling up and down using Java's `Robot` class.
- **Process Management:** Track and force-close the last opened application via voice.
- Recognizes voice and allows one to find a particular file on Desktop

## Prerequisites
1. **Java JDK 11+** installed.
2. **Vosk AI Model:** Download a lightweight model (e.g., `vosk-model-small-en-us-0.15`) from [Vosk Models](https://alphacephei.com/vosk/models) and extract it to a `models/` directory in your project root.

## How to Run
1. **Clone the repository:**
   ```bash
   git clone [git clone https://github.com/krishpatel-13/voice-recognizer-with-addition-features.git]
