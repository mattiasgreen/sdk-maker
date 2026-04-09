# Project Roadmap: Zero-Dependency Java SDK Generator

This document tracks our progress and outlines the iterative steps required to achieve a pristine, zero-dependency generated SDK.

## ✅ Completed Phases

**Phase 1: Project Scaffolding & Baseline Generation**
- Initialized a single-project Gradle build (`build.gradle.kts`) targeting Java 21.
- Hooked in the `org.openapi.generator` (v7.21.0) task to output source code into a browsable `generated-sdk/` directory.
- Verified that basic model generation and compilation succeed (temporarily utilizing Jackson and Jakarta dependencies).
- Established strict project rules in `AGENTS.md` and `README.md`.

**Phase 2: Template Extraction Infrastructure**
- Implemented a native `extractTemplates` Gradle task using `JavaExec` to download and run the OpenAPI CLI on the fly.
- Configured the extractor to dump baseline templates safely into `build/extracted-templates/` to avoid polluting version control or accidentally overriding the active build.

---

## 🚀 Upcoming Phases

### Phase 3: Dependency Purge & Setup
- Set `serializationLibrary = "none"` in the OpenAPI generation configuration.
- Remove all temporary Jackson and Jakarta imports from `build.gradle.kts`.
- Copy the baseline templates (e.g., `pojo.mustache`, `ApiClient.mustache`) from `build/extracted-templates/` into our active `src/templates/` directory.

### Phase 4: Model Simplification (Zero-Dep POJOs)
- Aggressively strip `pojo.mustache` of all legacy validation and serialization annotations.
- Verify generated models compile perfectly with no external libraries.

### Phase 5: Client Simplification (Native HttpClient)
- Refactor `ApiClient.mustache` and related API invocation templates.
- Ensure the underlying networking engine exclusively leverages `java.net.http.HttpClient`.
- Strip away redundant error handling or legacy fallback code mapping to OkHttp/Gson.

### Phase 6: E2E Verification
- Utilize the newly stripped-down generated client to make validated HTTP calls.
- Run integration tests (e.g., against WireMock) to prove standard JSON payloads parse accurately into the new Java 21 Records using only pure Java.
