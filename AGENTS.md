# Agent Instructions & Guidelines

This document provides instructions, constraints, and context for any AI coding agents (or human pair-programmers) contributing to the **Zero-Dependency Java SDK Generator** project.

## Project Context
- **Goal**: Architect a customizable Gradle build that generates a modern, Java SDK directly from an OpenAPI specification (`openapi.yaml`).
- **Core Constraints**: The final generated code must have **zero** third-party dependencies. It must exclusively map to standard libraries (e.g., `java.net.http.HttpClient` and `java.time`).
- **Environment**: JDK 25 running Gradle, targeting JDK 21 for the generated code.

---

## Technical Directives

### 1. Dependency Management (Strict)
- **Do not introduce external libraries** to the generated SDK's runtime footprint under any circumstances (No Jackson, Gson, OkHttp, RxJava, etc.).
- You may use dependencies in the root `build.gradle.kts` **only** for the generation process (e.g., the `org.openapi.generator` Gradle plugin) or for test compilation (e.g., JUnit, WireMock).

### 2. JDK 21+ Language Features
- **Records**: Use Java `Record` types instead of standard classes for Data Transfer Objects (DTOs) whenever possible to guarantee immutability.
- **Pattern Matching & Switch Expressions**: Leverage modern control flow structures in generated logic.
- **Native HTTP**: Exclusively utilize `java.net.http.HttpClient` for the underlying network execution logic.

### 3. Build & Directory Structure
- The build orchestrator is defined in the root `build.gradle.kts` (Kotlin DSL). 
- **Template Location**: Any customization to generated files must occur via Mustache template overrides strictly located in `src/templates/`.
- **Generated Artifacts**: Always route the output of the OpenAPI generator plugin to `build/generated-sdk/`. This directory is ephemeral and should never be committed to source control.

---

## Agent Runbook & Workflow Best Practices

When tasked with implementing features, follow this incremental workflow:

### Step 1: Analyze & Extract
If modifying how a specific file is generated (like an ApiClient or a POJO), do not guess the template syntax.
- Run the extraction command (e.g., via Docker or the OpenAPI CLI binaries) to dump the original base templates.
- Copy the target template (e.g., `pojo.mustache`) into `src/templates/`.

### Step 2: Strip & Refactor
- Remove irrelevant imports from the Mustache files (e.g., `import com.fasterxml.jackson...`).
- Simplify the Mustache logic. The OpenAPI default templates are notoriously bloated to support dozens of edge cases and legacy frameworks. Aggressively prune any conditions referencing `gson`, `jackson`, or `jakarta`/`javax` validation.

### Step 3: Verify the Build Graph
Always verify that your modified logic wires cleanly into the Gradle lifecycle.
- Run `./gradlew clean build` to verify that `openApiGenerate` executes prior to Java compilation.
- Ensure the generated code compiles cleanly under JDK 21.

### Step 4: Validate Classpath Purity
- Run `./gradlew dependencies` on the project.
- Verify that the runtime classpath contains absolutely no external dependencies.
- A task is considered "Done" only when standard compilation succeeds and the dependency tree remains clean.
