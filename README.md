# Mock Engine

Mock Engine is a powerful tool for mocking Ktorfit API responses in your Kotlin Multiplatform projects. It uses KSP (Kotlin Symbol Processing) to generate mock implementations of your Ktorfit APIs, making it easy to test your application without relying on a live server.

## How it works

Mock Engine works by processing annotations on your Ktorfit API interfaces. When you annotate a Ktorfit method with `@Mock`, Mock Engine generates a mock implementation of that method that returns data from a specified JSON file.

### Example

Here's an example of how to use Mock Engine to mock a Ktorfit API:

```kotlin
// In your Ktorfit API interface
interface SampleApi {
    @Mock(files = ["mock/sample-mock.json"])
    @GET("sample-url/{customPath}")
    suspend fun sampleRequest(@Path("customPath") customPath: String): SampleModel
}
```

In this example, the `@Mock` annotation tells Mock Engine to generate a mock implementation of the `sampleRequest` method that returns data from the `mock/sample-mock.json` file.

## Features

*   **Easy to use:** Simply annotate your Ktorfit API methods with `@Mock` to generate mock implementations.
*   **Flexible:** Mock different responses for the same API method by using different JSON files.
*   **Powerful:** Mock Engine uses KSP to generate mock implementations at compile time, so there's no runtime overhead.

## How to get started

To get started with Mock Engine, you'll need to add the following plugin and dependencies to your project:

**`build.gradle.kts`**

```kotlin
plugins {
    id("com.google.devtools.ksp")
    id("io.github.pedro-bachiega.mock-engine") version "<version>"
}

dependencies {
    // Ktorfit
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:<version>")

    // Mock Engine
    implementation("com.pedrobneto.mock.engine:client:<version>")
    implementation("com.pedrobneto.mock.engine:annotation:<version>")
}
```

Once you've added the plugin and dependencies, you can start annotating your Ktorfit API methods with `@Mock`.

## How to contribute

Mock Engine is an open-source project, and we welcome contributions from the community. If you'd like to contribute, please fork the repository and submit a pull request.
