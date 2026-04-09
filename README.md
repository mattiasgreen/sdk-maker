# Project Specification: Zero-Dependency Java SDK Generator

**Objective:** Bootstrap a Gradle project that utilizes the OpenAPI Generator plugin to produce a vanilla Java 21 SDK. The generated SDK must use the native `java.net.http.HttpClient`, support Builder patterns, and have exactly zero third-party dependencies (no Jackson, Gson, or validation libraries).

## 1. Directory Structure
The project should follow a standard Gradle layout with a dedicated directory for overriding Mustache templates. The actual SDK output will be routed to the `build/` directory so it isn't checked into version control.

```plaintext
zero-dep-sdk-generator/
├── .gitignore
├── build.gradle.kts                 # Main build script (Kotlin DSL preferred)
├── settings.gradle.kts              # Project name setup
├── openapi.yaml                     # The API specification source
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/sdk/     # Manual additions (e.g., the JsonSerializer SPI interface)
│   └── templates/                   # Extracted and modified OpenAPI templates
│       ├── pojo.mustache            # Stripped of JSON annotations
│       ├── ApiClient.mustache       # Modified to use ServiceLoader for the SPI
│       ├── build.gradle.mustache    # Emptied/Ignored (we manage the build via the root project)
│       └── pom.xml.mustache         # Emptied/Ignored
└── build/                           # (Git ignored)
    └── generated-sdk/               # The output directory of the OpenAPI Generator plugin
```

## 2. Gradle Build Setup (build.gradle.kts)
The build script needs to accomplish three things: apply the OpenAPI plugin, configure the generation task with the correct flags, and tell the Java compiler where to find the generated source code.

**Plugins:**
- `java-library`
- `org.openapi.generator` (Version 7.5.0 or latest stable)

**Java Toolchain:**
- Target JDK 21.

**OpenAPI Task Configuration (openApiGenerate):**
Instruct the agent to configure the task with these specific properties to achieve our architectural goals:

```kotlin
plugins {
    `java-library`
    id("org.openapi.generator") version "7.5.0" // Use latest 7.x
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// 1. Configure the Generation Task
tasks.openApiGenerate {
    generatorName.set("java")
    library.set("native") // Uses java.net.http.HttpClient
    inputSpec.set("$rootDir/openapi.yaml")
    templateDir.set("$rootDir/src/templates")
    outputDir.set("$buildDir/generated-sdk")
    apiPackage.set("com.example.sdk.api")
    modelPackage.set("com.example.sdk.model")
    invokerPackage.set("com.example.sdk.invoker")
    
    configOptions.set(mapOf(
        "generateBuilders" to "true",         // Native builder pattern
        "dateLibrary" to "java8",             // Use java.time.*
        "useJakartaEe" to "false",            // Prevent Jakarta validation annotations
        "serializationLibrary" to "none",     // Crucial: disables default Jackson/Gson wiring
        "openApiNullable" to "false"          // Removes jackson-databind-nullable dependency
    ))
}

// 2. Wire Generated Source into Compilation
sourceSets {
    main {
        java {
            srcDir("$buildDir/generated-sdk/src/main/java")
        }
    }
}

// 3. Ensure Generation Happens Before Compilation
tasks.withType<JavaCompile> {
    dependsOn(tasks.openApiGenerate)
}

// 4. Dependencies
dependencies {
    // Deliberately empty! Zero transitive dependencies.
    // Junit/Test implementations can be added here later.
}
```

## 3. Agent Instructions for Template Extraction
To give the agent a complete starting point, instruct it to extract the base templates into the `src/templates` directory so they are ready for the dependency purge.

**Agent Execution Steps:**

1. Initialize the Gradle project with the provided `build.gradle.kts`.
2. Create a placeholder `openapi.yaml` with a single simple endpoint for testing.
3. Run the OpenAPI CLI (via Docker or local binary) to dump the baseline templates:
   `openapi-generator generate -g java --library native -i openapi.yaml -o /tmp/dump -t src/templates` (or equivalent extraction command).
4. Copy ONLY `pojo.mustache` and `ApiClient.mustache` into the project's `src/templates/` directory.

## 4. Definition of Done for the Agent
The agent's task is considered complete when:

1. Running `./gradlew build` successfully triggers the `openApiGenerate` task.
2. The output inside `build/generated-sdk/src/main/java` compiles successfully under JDK 21.
3. Running `./gradlew dependencies` shows an entirely empty runtime classpath.
