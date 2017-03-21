gradle-rocker-plugin
=================

# Overview

[Gradle](http://www.gradle.org) plugin that integrates the Rocker template engine.

The rocker plugin is hosted at [Bintray's JCenter](https://bintray.com/etienne/gradle-plugins/gradle-rocker-plugin).

# Goals

...
 
# Functionality

The following functionality is provided by the rocker plugin:
 
 * ...
 
# Design

...

# Configuration

## Apply rocker plugin

Apply the `nu.studer.rocker` plugin to your Gradle project.

### Gradle 1.x and 2.0

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'nu.studer:gradle-rocker-plugin:0.1'
    }
}

apply plugin: 'nu.studer.rocker'
```

### Gradle 2.1 and higher

```groovy
plugins {
  id 'nu.studer.rocker' version '0.1'
}
```

Please refer to the [Gradle DSL PluginDependenciesSpec](http://www.gradle.org/docs/current/dsl/org.gradle.plugin.use.PluginDependenciesSpec.html) to 
understand the behavior and limitations when using the new syntax to declare plugin dependencies.

# Invocation

## Invoke rocker-make

...

# Example

You can also find a self-contained example build script [here](example/build.gradle).

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
