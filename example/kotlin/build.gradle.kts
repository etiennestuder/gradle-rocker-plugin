import nu.studer.gradle.rocker.RockerConfig

plugins {
    id("nu.studer.rocker") version "2.1"
    id("java")
}

repositories {
    jcenter()
}

val rockerVersion by extra("1.3.0")

rocker {
    create("main") {
        templateDir = file("src/rocker")
        outputDir = file("src/generated/rocker")
        isOptimize = true // optional
    }
}
