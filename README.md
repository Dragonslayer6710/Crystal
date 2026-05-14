# Crystal

Crystal is a Java/LWJGL game engine project with a separate sandbox application for testing engine features.

## Project Structure

```text
crystal-engine/     Reusable engine runtime code
crystal-sandbox/    Demo application and project-specific assets
gradle/             Root Gradle wrapper files
```

Use the root Gradle wrapper for all builds:

```powershell
.\gradlew.bat build
```

## Requirements

- Java 21
- Windows runtime for now

LWJGL native dependencies are currently configured for Windows in `crystal-engine/build.gradle`.

## Assets

Engine-owned rendering assets are bundled with `crystal-engine`:

```text
crystal-engine/src/main/resources/engine-assets/
```

Project/demo assets live in the sandbox:

```text
crystal-sandbox/assets/
```

This keeps required engine shaders, such as skybox and IBL generation shaders, separate from game-specific shaders like the sandbox `basic` shader.

## Running The Sandbox

```powershell
.\gradlew.bat :crystal-sandbox:run
```

The sandbox asset root is configured in `SandboxMain`.

## Development Notes

`gradle.properties` intentionally contains local SSL bypass settings for dependency resolution. Keep that decision explicit if the project is shared or moved between machines.
