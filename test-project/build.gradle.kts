plugins {
    java
    id("io.github.bennofs.continuous-exec").version("0.0.1")
}

dependencies {
    compileOnly(gradleApi())
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
    javaExec {
        main = "com.example.ContinuousExample"
        classpath = sourceSets["main"].runtimeClasspath
    }

    watch.from("build/example-trigger")
    watch.from(createDirTask)
}

sourceSets["main"].runtimeClasspath.forEach {
    println(it.toString())
}



