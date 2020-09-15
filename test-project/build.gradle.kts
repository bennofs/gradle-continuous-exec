plugins {
    java
    id("io.github.bennofs.continuous-exec").version("+")
}

dependencies {
    implementation("org.json:json:20200518")
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    jcenter()
    maven { url = uri("https://jitpack.io") }
}

val createDirTask by tasks.register("create-dir") {
    outputs.dir("build/example-dir")
    doLast {
        mkdir("build/example-dir")
    }
}

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

sourceSets["main"].runtimeClasspath.forEach {
    println(it.toString())
}



