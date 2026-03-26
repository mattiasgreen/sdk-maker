# Java SDK Maker

A highly opinionated, zero-configuration tool for generating pristine, modern Java SDKs. 

Under the hood, this project stands on the shoulders of the de facto standard [OpenAPI-Generator](https://github.com/OpenAPITools/openapi-generator), but completely reimagines the generated output and developer experience for modern Java.

## The "Why" - A Critical Appraisal

**If OpenAPI-Generator already exists, why build this wrapper?**

The OpenAPI-Generator is incredibly powerful and handles the nightmarish edge cases of the OpenAPI specification beautifully. However, its default Java outputs and configuration experience leave much to be desired for modern enterprise development:
- The default Java templates are bloated with legacy patterns (mutable POJOs, massive getters/setters, `equals`/`hashCode`).
- Generated code heavily relies on third-party runtime dependencies (Gson, OkHttp, Jackson, RxJava, etc.).
- Configuring the generator to use custom type mappings, import mappings, and template overrides usually requires a massive block of boilerplate in your `build.gradle` or CLI scripts.

### The Value Proposition
This SDK Maker acts as a highly curated orchestrator. We reuse the heavy-lifting parsing and semantic AST engine of OpenAPI-Generator, but override its behavior entirely to provide:

1. **Zero Third-Party Dependencies in Generated Code:** Our custom template overrides output pure modern Java. We exclusively target `java.net.http.HttpClient` and standard library components. By ditching OkHttp and Jackson/Gson, the generated SDKs are transparent and incredibly lightweight.
2. **First-Class Modern Idioms:** We force the generator to emit `Records` for DTOs and `sealed interfaces` for polymorphism (`oneOf`/`anyOf`). The output is immutable-by-default and highly readable.
3. **"Golden Path" Configuration:** As a consumer, you shouldn't have to map standard OpenAPI date types to `java.time` manually or configure complex Mustache template directories. This library abstracts the OpenAPI-Generator programmatic API behind a single, beautiful "plug-and-play" builder configuration. 

## High-Level Requirements & Trade-offs

### The Build Environment: JDK 25 & Gradle 9
- **Why JDK 25?** Gives us the absolute latest language features (pattern matching, latest collections) for writing the orchestrator layer and our custom `CodegenConfig` extensions.
- **Why Gradle 9?** Standardize around modern declarative builds (`build.gradle.kts`, version catalogs).

### The Target Output: JDK 17 (The Sweet Spot)
- **Why JDK 17?** It's the standard LTS baseline for most modern enterprise applications (Spring Boot 3+ requires it). It gives the generated SDK access to `Records`, Text Blocks, and `java.net.http.HttpClient` natively. 
- **The JDK 8/11 Trade-off:** We intentionally abandon pre-17 codebases. Supporting them means falling back to bloated POJOs and tying the SDK consumer to third-party HTTP clients, which ruins our "modern, zero-dependency" value proposition. 

## Architecture & Project Structure

By embedding `org.openapitools:openapi-generator` as a library, we can programmatically inject our customizations without forcing users to mess with CLI flags.

```text
sdk-maker/
├── sdk-maker-core/      # The orchestrator: wraps openapi-generator, injects our custom templates/mappings
├── gradle-plugin/       # A clean, minimal Gradle plugin wrapping sdk-maker-core
├── templates/           # (Internal) Our curated Mustache/Handlebars templates for Java 17+
└── examples/            # Our validating "Dogfooding" directory
    └── customer-sdk/    # Validates against a complex Bank "Customers API"
        ├── library/     # The generated production SDK code
        └── testkit/     # The generated mock data builders and test fixtures
```

## Validation & Testkit Strategy

A high-quality SDK doesn't just provide production code; it also provides tooling for the *consumers* to test their own applications without hitting the real API. 

Therefore, our generation engine must support a **Testkit Output Strategy**:

1. **Test Data Builders (Mother Object Pattern):** For every model object generated (e.g., `Customer`, `Address`), the generator should also create a fluent Test Data Builder in a separate module/source-set. This allows consumers to easily spawn mock domain objects initialized with safe default values (e.g., `CustomerBuilder.validCustomer().withStatus(ACTIVE).build()`).
2. **WireMock Integration Validation:** In our own validating projects (like the `customer-sdk` example), we must prove the generated SDK works by writing true JUnit integration tests against a local `WireMock` server. 
3. **Using Builders for Stubs:** The integration tests should **never** rely on raw JSON string literals or external files for mocking API responses. Instead, the test should use the *generated Testkit builders* to instantiate the mocked response objects, and then let Jackson serialize those objects directly into the WireMock `stubFor` definition. If our generated model objects cannot securely round-trip through Jackson serialization, the test fails.

## Scope Boundaries & "Gotchas"

SDK generators can quickly derail when trying to support every edge-case. For Version 1, here are our strict scope boundaries concerning common generator pain points:

### 1. The Enum Evolution Problem (Strict Evaluation)
**Decision:** Strict evaluation.
APIs often add new enum values (e.g., a new `CustomerStatus.DELETED`). In Java, strict `enum` types crash Jackson deserialization if an unknown value is encountered. We make the deliberate choice that API enum additions are breaking changes for the client. We will generate strict Java enums; if the upstream API shifts, the consumer must update to a newly generated SDK version.

### 2. Error Handling & Problem Details
**Decision:** Business Exceptions over HTTP Codes.
When an API returns an error (e.g., a 404 because a service was misrouted or a resource is missing), the consumer shouldn't have to parse bare `HTTP_NOT_FOUND` integers. The generated SDK should map recognized API errors into meaningful, unchecked business exceptions (e.g., `CustomerNotFoundException`). Undocumented failures should fall back to a generic `SystemException`. We will establish base exception hierarchies within our generated footprint.

### 3. Resilience & Retries (Stretch Goal)
**Decision:** Native execution first; extensible backpressure later.
Because we use native `java.net.http.HttpClient`, we don't get OkHttp's automatic network retries. For MVP, we defer resilience logic. As a stretch goal, we will design the client builder to allow consumers to plug in their own backoff, retry, and circuit-breaker mechanics (likely via a request-interceptor interface).

### 4. Authentication Integration (Later)
**Decision:** Unauthenticated initial testing.
The SDK architecture will eventually require support for injecting headers/auth tokens. For MVP and current validation, the `Customer API` calls will be unauthenticated.

### 5. Sync vs. Async Execution (Sync First)
**Decision:** Synchronous only.
While the native HttpClient handles async beautifully, we will generate standard, blocking synchronous methods first to keep the initial templates simple. Async is a stretch goal.

### 6. Pagination (Keep It Simple)
**Decision:** Consumer-managed.
Auto-fetching iterators or lazy streams for `GET /customers` are out of scope. The SDK returns the raw page response; the consumer writes the `while` loop and tracks the cursor.
