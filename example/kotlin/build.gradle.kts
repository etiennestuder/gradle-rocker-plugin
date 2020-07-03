import nu.studer.gradle.rocker.RockerConfig

plugins {
    id("nu.studer.rocker") version "2.1.1"
    id("java")
}

repositories {
    jcenter()
}

val rockerVersion by extra("1.3.0")

rocker {
    create("main") {
        templateDir.set(file("src/rocker"))
        outputDir.set(file("src/generated/rocker"))
        optimize.set(true) // optional
    }
}
