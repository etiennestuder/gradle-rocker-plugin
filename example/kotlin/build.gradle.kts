plugins {
    id("nu.studer.rocker") version "3.2"
    id("java")
}

repositories {
    mavenCentral()
}

rocker {
    version.set("2.2.1")
    configurations {
        create("main") {
            optimize.set(true) // optional
            discardLogicWhitespace.set(true) // optional
            templateDir.set(file("src/rocker"))
            outputDir.set(file("src/generated/rocker"))
        }
    }
}
