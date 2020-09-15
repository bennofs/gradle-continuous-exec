# continuous-exec gradle plugin

A plugin for gradle to spawn persistent daemon processes in a continuous build. 
The daemon processes receive notifications when files change.

## Usage

To use this plugin, add the following to your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.bennofs.continuous-exec").version("0.1.0")
}
```

You can then define continuous java exec tasks:

```kotlin
tasks.register<io.github.bennofs.gradle.continuous.ContinuousJavaExec>("example") {
    /* here, all options from JavaExec are supported */
    javaExec {
        main = "com.example.ContinuousExample"
        classpath = sourceSets["main"].runtimeClasspath
    }

    /* watch is used to specify the directories and files that are watched for changes
     * only files listed here will be passed as changed paths to the daemon process,
     * but a reload will also be triggered if any of the task dependencies change.
     */
    watch.from("build/example-trigger")
    watch.from(createDirTask)
}
```

See the files in `test-project/` for a full working example.