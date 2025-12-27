plugins {
    id("nu.studer.rocker") version "3.3"
    id("java")
}

repositories {
    mavenCentral()
}

rocker {
    version.set("2.4.0")
    configurations {
        create("main") {
            optimize.set(true) // optional
            discardLogicWhitespace.set(true) // optional
            templateDir.set(file("src/rocker"))
            outputDir.set(file("src/generated/rocker"))
        }
    }
}
