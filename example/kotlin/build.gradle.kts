plugins {
    id("nu.studer.rocker") version "3.0.1"
    id("java")
}

repositories {
    jcenter()
}

rocker {
    version.set("1.3.0")
    configurations {
        create("main") {
            optimize.set(true) // optional
            templateDir.set(file("src/rocker"))
            outputDir.set(file("src/generated/rocker"))
        }
    }
}
