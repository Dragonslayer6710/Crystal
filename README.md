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

LWJGL native dependencies are selected automatically for Windows, Linux, and
macOS. Override the detected classifier when needed:

```powershell
.\gradlew.bat build -PlwjglNatives=natives-windows
```

## Assets

Engine-owned rendering assets are bundled with `crystal-engine`:

```text
crystal-engine/src/main/resources/engine-assets/
```

Project/demo assets live in the sandbox:

```text
crystal-sandbox/assets/
```

This keeps required engine shaders, such as skybox and IBL generation shaders, separate from game-specific shaders like the sandbox `pbr` shader.

## Running The Sandbox

```powershell
.\gradlew.bat :crystal-sandbox:run
```

The sandbox asset root is configured in `SandboxMain`.

## Development Notes

`gradle.properties` intentionally contains local SSL bypass settings for dependency resolution. Keep that decision explicit if the project is shared or moved between machines.

Dependency versions are centralized in `gradle.properties`. Snapshot dependency
resolution is disabled by default; set `useSnapshotRepository=true` only when a
snapshot dependency is being tested intentionally.
