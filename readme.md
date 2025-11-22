# Quiz App

A modern, feature-rich Android Quiz application built entirely with Jetpack Compose. This project demonstrates best practices in Android development, including a clean MVVM architecture, asynchronous networking, and a delightful user experience with animations and gestures.

## Features

*   **Modern UI with Jetpack Compose**: A fully declarative UI built with the latest Android toolkit.
*   **Material 3 Dark Theme**: A beautiful and accessible dark theme that is easy on the eyes.
*   **Streak Logic**: Track consecutive correct answers. The streak counter "lights up" with a special animation after 3 correct answers in a row.
*   **Gestures**: Swipe left on a question to skip it.
*   **Robust State Management**: Uses a single `QuizUiState` to manage the entire UI state, preventing bugs and ensuring a consistent experience.
*   **Comprehensive Results Screen**: View your final score, percentage, longest streak, and number of skipped questions.

## Tech Stack

*   **Language**: Kotlin
*   **UI Toolkit**: Jetpack Compose
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Asynchronous**: Kotlin Coroutines
*   **Networking**: Retrofit2 & Gson
*   **Navigation**: Navigation Compose

## Architecture

This app follows the **MVVM (Model-View-ViewModel)** architecture to ensure a clean separation of concerns.

*   **Model (`data`)**: Contains the `Question` data class, `QuizRepository` for fetching data, and `RetrofitClient` for network setup.
*   **ViewModel (`ui`)**: The `QuizViewModel` holds and manages the UI state (`QuizUiState`). It handles all business logic and exposes the state as a `StateFlow` for the UI to observe.
*   **View (`ui/screens`)**: Stateless Composable functions (`QuizScreen`, `ResultsScreen`, etc.) that observe the `ViewModel`'s state and render the UI.

## Project Structure

The project is organized into clear packages to maintain a clean and scalable structure.
