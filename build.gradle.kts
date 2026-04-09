plugins {
    `java-library`
    id("org.openapi.generator") version "7.21.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.openApiGenerate {
    generatorName.set("java")
    library.set("native")
    inputSpec.set("$rootDir/openapi.yaml")
    outputDir.set("$buildDir/generated-sdk")
    apiPackage.set("com.example.sdk.api")
    modelPackage.set("com.example.sdk.model")
    invokerPackage.set("com.example.sdk.invoker")
    configOptions.set(mapOf(
        "useJakartaEe" to "true"
    ))
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated-sdk/src/main/java")
        }
    }
}

tasks.withType<JavaCompile> {
    dependsOn(tasks.openApiGenerate)
}

repositories {
    mavenCentral()
}

dependencies {
    // Temporary dependencies to allow standard code generation to compile
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

}
