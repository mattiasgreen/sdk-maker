plugins {
    `java-library`
    id("org.openapi.generator") version "7.21.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val openapiCli by configurations.creating

tasks.openApiGenerate {
    generatorName.set("java")
    library.set("native")
    inputSpec.set("$rootDir/openapi.yaml")
    outputDir.set("$rootDir/generated-sdk")
    apiPackage.set("com.example.sdk.api")
    modelPackage.set("com.example.sdk.model")
    invokerPackage.set("com.example.sdk.invoker")
    templateDir.set("$rootDir/src/templates")
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to ""
    ))
    ignoreFileOverride.set("$rootDir/.openapi-generator-ignore")
    configOptions.set(mapOf(
        "annotationLibrary" to "none"
    ))
}

tasks.clean {
    delete("$rootDir/generated-sdk")
}

sourceSets {
    main {
        java {
            srcDir("$rootDir/generated-sdk/src/main/java")
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
    "openapiCli"("org.openapitools:openapi-generator-cli:7.21.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.wiremock:wiremock:3.5.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("extractTemplates") {
    group = "openapi tools"
    description = "Extracts default OpenAPI generator templates to build/extracted-templates."
    classpath = openapiCli
    mainClass.set("org.openapitools.codegen.OpenAPIGenerator")
    args = listOf(
        "author",
        "template",
        "-g", "java",
        "--library", "native",
        "-o", "$buildDir/extracted-templates"
    )
}
