# Zero-Dependency Java SDK Generator

A highly opinionated, Gradle-based toolkit for generating pristine, modern Java 21 SDKs directly from an OpenAPI specification, completely free of third-party runtime dependencies.

## Overview

Most OpenAPI generators produce bulky Java client code heavily reliant on libraries like Jackson, Gson, OkHttp, or RxJava for networking, serialization, and annotations. This project leverages the robust parsing ecosystem of [OpenAPI-Generator](https://github.com/OpenAPITools/openapi-generator) but aggressively overrides its templates to output pure, "vanilla" Java 21 code.

### Core Objectives
- **Zero Transitive Dependencies:** The final generated SDK runtime classpath contains only standard Java components.
- **Modern Java Idioms:** Leverages the native `java.net.http.HttpClient` and standard `java.time` packages.
- **Clean Models:** Generated DTOs prioritize modern Java features, stripping away legacy boilerplate, massive getters/setters, and heavy mapping annotations.

## Project Structure

- `openapi.yaml`: The source of truth API specification.
- `build.gradle.kts`: The primary build orchestrator. Uses the OpenAPI generator plugin to automate code creation and immediately compiles the fresh code.
- `src/templates/`: Highly optimized, custom Mustache template overrides. Any manipulation to the default OpenAPI generator template behavior is done here.
- `generated-sdk/`: The final destination of the generated source code. This directory is ephemeral, git-ignored, and freshly populated prior to Java compilation.

## Getting Started

1. Set up your API definitions in `openapi.yaml`.
2. Edit or introduce new templates within `src/templates/` to refine the generated output structure.
3. Run the generator and build the SDK:

```bash
./gradlew build # builds the project, including code generation
./gradlew extractTemplates # optional, to extract default templates for customization
./gradlew clean # optional, to clean the generated code
./gradlew test # optional, to run integration tests, uses wiremock to mock the API
```
