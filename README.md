# Welcome to Group 11s Technical Documentation

Product Name: Sound Collage<br>
Version: Iteration 2<br>
Phase: Development <br>
Date: Dec 15, 2023<br>

The sound collage is a sound editor for wav files that creates a collage of sounds 
in Java - [here's how it works](#running-the-development-server) <br>

## Index

1. [Introduction](#introduction)
    - 1.1 [Overview](#overview)
    - 1.2 [Features](#features)
2. [Technical Stack](#technical-stack)
    - 2.1 [Java](#java)
    - 2.2 [Java-FX](#java-fx)
    - 2.3 [Maven](#maven)
3. [Project Structure](#project-structure)
4. [Installation](#installation)
5. [Dependencies](#dependencies)
6. [Development](#development)
    - 6.1 [Running the Development Server](#running-the-development-server)
    - 6.2 [Code Guidelines](#code-guidelines)
7. [Testing](#testing)
    - 7.1 [Unit Tests](#unit-tests)
11. [Deployment](#deployment)
    - 11.1 [Build](#build)
    - 11.2 [Deploying to Production](#deploying-to-production)
12. [Appendices](#appendices)
    - 12.1 [TarsosDSP](#tarsosdsp)
    - 12.2 [JavaFX](#javafx)
13. [Troubleshooting](#troubleshooting)
14. [Contributing](#contributing)
15. [License](#license)

## 1. Introduction

### 1.1 Overview

The sound collage is an application that enables editing of wav files and merging to construct a collage

### 1.2 Features

- **Merging:** Combine 2 wav files together to create a collage
- **Waveform:** Realtime audio wave form data for wav files
- **Reverse Sound:** Reverse the audio file to play backwards in realtime.
- **Pitch:** Adjust pitch of individual sound files in realtime.
- **Volume:** Change global system volume for build file in realtime.
- **Equalizer:** 


## 2. Technical Stack

### 2.1 Java

Java is the codebase the project is built on, utilising the standard sound library for audio manipulation

### 2.2 Java FX

Java FX is a powerful library for building graphical user interfaces

### 2.3 Maven

Project is configured with maven for building, testing and running the application for easy installation of dependencies


## 3. Project Structure

The project is organized into clear and intuitive directories: <br>
**`project/src/`**
- **`main`:** application source files
- **`test`:** test components for unit testing

**`project/src/main`**
- **`java`:** java source files
- **`resources`:** sound files, build file location and user interface design

**`project/src/main/java/com.sound_collage`**
- **`controllers`:** MVC controller classes
- **`models`:** MVC model classes

**`project/src/main/resources`**
- **`com.sound_collage.view`:** MVC view javafx design
- **`sound_files`:** sound files upload and export to

## 4. Installation

To compile the project into a FAT jar and run

```bash

```
## 5. Dependencies

List and briefly describe the key dependencies used in the project.

## 6. Development

### 6.1 Compiling in Intellij

To compile the project in intellij
```bash
npm run dev
```

### 6.2 Code Guidelines

The codebase adheres to industry-standard coding conventions and follows the guidelines outlined in the project's contributing documentation.

## 8. Testing

### 8.1 Unit Tests

To run unit tests, navigate to the model_test folder
```bash

```

## 9. Deployment

### 9.1 Build

To build the project into a FAT jar for production, execute:

```bash

```

### 9.2 Deploying to Production

Deploy the application to a production environment using the deployment instructions outlined in the project's deployment documentation.

## 10. Appendices

### 10.1 JavaFX

JavaFX library for GUI [JavaFX](https://openjfx.io/openjfx-docs/)

### 10.2 TarsosDSP

Tarsos library for manipulating the pitch [TarsosDSP](https://0110.be/tags/TarsosDSP)


## 11. Troubleshooting

Offer solutions to common issues or errors that developers might encounter.

## 12. Contributing

Provide guidelines for developers who wish to contribute to the project.

## 13. License

Specify the license under which the project is released.
