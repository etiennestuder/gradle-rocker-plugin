gradle-rocker-plugin
=================

# Overview

[Gradle](http://www.gradle.org) plugin that integrates the Rocker template engine. For each named rocker configuration declared
in the build, the plugin adds a task to generate the Java sources from the specified Rocker templates and includes the 
generated Java sources in the matching source set, if existing. The code generation tasks participate in incremental builds and 
in task output caching by the [Gradle build cache](https://docs.gradle.org/current/userguide/build_cache.html). The plugin can be 
applied on both Java projects and Android projects.

You can find out more details about the actual Rocker source code generation in the [Rocker documentation](https://github.com/fizzed).

The rocker plugin is hosted at [Bintray's JCenter](https://bintray.com/etienne/gradle-plugins/gradle-rocker-plugin).
 
# Functionality

The following functionality is provided by the rocker plugin:
 
 * Generate Java sources from a given set of Rocker templates
 * Add the generated Java sources to the name-matching source set, if existing
 * Wire task dependencies such that the Java sources are generated before the Java compile task of the name-matching source set compiles them, if existing  
 * Add a task to clean the generated sources as part of the `clean` life-cycle task

The following configuration changes are provided by the rocker plugin:
 
 * Add the `com.fizzed:rocker-compiler` dependency needed to execute the Rocker template engine to the new `rockerCompiler` configuration  
 * Add the `com.fizzed:rocker-runtime` dependency to the name-matching `compile` configuration to successfully compile the Java sources generated from the Rocker templates
 * Use the customizable Rocker version across all `com.fizzed:rocker-*` dependencies

The following Gradle features are supported by the rocker plugin:
 
 * `RockerCompile` task instances participate in incremental builds  
 * `RockerCompile` task instances participate in task output caching (if the rocker hot-reloading feature is disabled) 

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
        classpath 'nu.studer:gradle-rocker-plugin:0.2'
    }
}

apply plugin: 'nu.studer.rocker'
```

### Gradle 2.1 and higher

```groovy
plugins {
  id 'nu.studer.rocker' version '0.2'
}
```

Please refer to the [Gradle DSL PluginDependenciesSpec](http://www.gradle.org/docs/current/dsl/org.gradle.plugin.use.PluginDependenciesSpec.html) to 
understand the behavior and limitations when using the new syntax to declare plugin dependencies.

## Define rocker configurations

This is a sample configuration:
 
```groovy
plugins {
    id 'nu.studer.rocker' version '0.2'
    id 'java'
}

repositories {
    jcenter()
}

rocker {
  main {
    templateDir = file('src/rocker')
    outputDir = file('src/generated/rocker')
    optimize = true  // optional
  }
}

rockerVersion = '0.18.0'  // optional
```

The rocker _main_ configuration declares that the Rocker templates are in _src/rocker_ and the generated Java sources need to end up in _src/generated/rocker_. It further 
declares via the _optimize_ property that the generated Java sources should be optimized to not contain any code that allows for hot-reloading via Rocker. Since the name 
of the configuration is `main`, the generated sources are added to the `main` source set contributed by the applied `java` plugin.

Given the configuration above, you can invoke the Rocker template engine by issuing `./gradlew compileRocker`. You can also directly call `./gradlew compileJava` which first
generates the Java sources from the Rocker templates, and then compiles these Java sources as part of compiling all sources in the _main_ source set. 

Since we declared to use version _0.18.0_ of the Rocker template engine, all Rocker dependencies of all Gradle configurations will be of that given version. 

> I suggest you use the [Continuous build](https://docs.gradle.org/current/userguide/continuous_build.html) feature of Gradle instead of using the Rocker hot-reload feature.
> Declare `optimize = true` in the rocker configuration of your Gradle build, and then run your build with the `-t` command line option. Deactivating the hot-reload feature 
> of Rocker will also enable the rocker tasks for task output caching by the [Gradle build cache](https://docs.gradle.org/current/userguide/build_cache.html).

> Warning: do not configure any of `templateDir`, `outputDir`, and `classDir` to point to the same directory or to a directory that also contains other content.  
 
# Invocation

## Invoke rocker task

You can generate the Java sources for a given named configuration by invoking the command `compile<configName>Rocker`, e.g. `compileTestRocker`. The only exception being _main_
that is abbreviated to `compileRocker`, similarly to how it is done for the `JavaCompile` tasks contributed by the `java` plugin.

# Examples

You can find a self-contained example build script [here](example/build.gradle).

# Changelog

+ 0.2 - New DSL, more rocker configuration options, support for Gradle build cache
+ 0.1 - Initial version

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

None, yet.

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
