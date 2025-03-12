<p align="left">
  <a href="https://github.com/etiennestuder/gradle-rocker-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/gradle-rocker-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

gradle-rocker-plugin
====================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Overview

[Gradle](http://www.gradle.org) plugin that integrates the Rocker template engine.

For each named Rocker configuration declared in the build, the plugin adds a task to generate the Java sources from the specified Rocker templates and includes the
generated Java sources in the matching source set, if existing. The code generation tasks participate
in [task configuration avoidance](https://docs.gradle.org/current/userguide/task_configuration_avoidance.html),
in [build configuration caching](https://docs.gradle.org/nightly/userguide/configuration_cache.html),
in [task output caching](https://docs.gradle.org/current/userguide/build_cache.html),
and in [incremental builds](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks). Additionally,
the compile task itself is [incremental](https://docs.gradle.org/current/dsl/org.gradle.work.InputChanges.html), meaning it is optimized so that
only templates which have changed are regenerated. The plugin can be applied on both Java projects and Android projects.

You can find more details about the actual Rocker source code generation in the [Rocker documentation](https://github.com/fizzed/rocker).

The Rocker plugin is hosted at the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/nu.studer.rocker).

## Build scan

Recent build scan: https://scans.gradle.com/s/nrhggndytzjze

Find out more about build scans for Gradle and Maven at https://scans.gradle.com.

# Functionality

The following functionality is provided by the Rocker plugin:

 * Generate Java sources from a given set of Rocker templates
 * Add the generated Java sources to the name-matching source set, if existing
 * Wire task dependencies such that the Java sources are generated before the Java compile task of the name-matching source set compiles them, if existing

The following Gradle configuration changes are contributed by the Rocker plugin:

 * Add the `com.fizzed:rocker-compiler` dependency needed to execute the Rocker template engine to the new `rockerCompiler` configuration
 * Add the `com.fizzed:rocker-runtime` dependency to the name-matching `implementation` configuration to successfully compile the Java sources generated from the Rocker templates
 * Use the customizable Rocker version across all resolved `com.fizzed:rocker-*` dependencies

The following Gradle features are supported by the Rocker plugin:

 * `RockerCompile` task instances participate in task configuration avoidance
 * `RockerCompile` task instances participate in configuration caching
 * `RockerCompile` task instances participate in incremental builds
 * `RockerCompile` task instances are themselves incremental (if the Rocker hot reload feature is disabled)
 * `RockerCompile` task instances participate in task output caching (if the Rocker hot reload feature is disabled)

# Compatibility

|Plugin version|Compatible Gradle versions|Support for Gradle Kotlin DSL         |Support for Gradle Configuration Cache|
|--------------|--------------------------|--------------------------------------|--------------------------------------|
| 3.0+         | 6.1+                     | Yes                                  | Yes |
| 2.0+         | 6.0+                     | Yes                                  | Yes |
| 1.0.1        | 5.0+, 6.0+               | Yes                                  | No |

# Configuration

## Apply Rocker plugin

Apply the `nu.studer.rocker` plugin to your Gradle project.

```groovy
plugins {
    id 'nu.studer.rocker' version '3.0.5'
}
```

Please refer to the [Gradle DSL PluginDependenciesSpec](http://www.gradle.org/docs/current/dsl/org.gradle.plugin.use.PluginDependenciesSpec.html) to
understand the behavior and limitations when using the new syntax to declare plugin dependencies.

## Define Rocker configurations

This is a sample configuration:

```groovy
plugins {
    id 'nu.studer.rocker' version '3.0.5'
    id 'java'
}

repositories {
    mavenCentral()
}

rocker {
    version = '1.3.0'  // optional
    configurations{
        main {
            templateDir = file('src/rocker')
            outputDir = file('src/generated/rocker')
            optimize = true  // optional
        }
    }
}
```

The rocker _main_ configuration declares that the Rocker templates are in _src/rocker_ and the generated Java sources need to end up in _src/generated/rocker_. It further
declares via the _optimize_ property that the generated Java sources should be optimized to not contain any code that allows for hot reload via Rocker. Since the name
of the configuration is `main`, the generated sources are added to the `main` source set contributed by the applied `java` plugin.

Given the configuration above, you can invoke the Rocker template engine by issuing `./gradlew compileRocker`. You can also directly call `./gradlew compileJava` which first
generates the Java sources from the Rocker templates, and then compiles these Java sources as part of compiling all sources in the _main_ source set.

Since we declared to use version _1.3.0_ of the Rocker template engine, all Rocker dependencies of all Gradle configurations will be of that given version.

> I suggest you use the [Continuous build](https://docs.gradle.org/current/userguide/continuous_build.html) feature of Gradle instead of using the Rocker hot reload feature.
> Declare `optimize = true` in the Rocker configuration of your Gradle build, and then run your build with the `-t` command line option. In addition, deactivating the hot
> reload feature of Rocker will enable the Rocker tasks for task output caching by the [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html).

## Complete Rocker configuration options

For each named configuration, the following options can be configured:

  * `optimize` (boolean): if _true_, hot reload support is removed from the generated templates, task output becomes cacheable
  * `discardLogicWhitespace` (boolean): if _true_, discards lines consisting of only logic/block
  * `extendsClass` (String): the class that all template implementations should extend
  * `extendsModelClass` (String): the class that all template models should extend
  * `javaVersion` (String): the Java version that the templates' compile & runtime must be compatible with
  * `targetCharset` (String): the target charset of the generated Java sources
  * `templateDir` (Path): the base directory where Rocker recursively starts from when locating and parsing template files
  * `outputDir` (Path): the directory where Rocker will generate the Java sources into
  * `classDir` (Path): the directory where the hot reload feature will compile classes to at runtime

> Warning: do not configure any of `templateDir`, `outputDir`, and `classDir` to point to the same directory or to a directory that also contains other content.

# Invocation

## Invoke Rocker task

You can generate the Java sources for a given Rocker configuration by invoking the command `compile<configName>Rocker`, e.g. `compileTestRocker`. The only exception being _main_
that is abbreviated to `compileRocker`, similarly to how it is done for the `JavaCompile` tasks contributed by the `java` plugin.

# Examples

See the self-contained example build scripts for the [Groovy DSL](example/groovy) and the [Kotlin DSL](example/kotlin).

# Changelog

+ 3.0.6 - TBD
+ 3.0.5 - Add discardLogicWhitespace configuration option.
+ 3.0.4 - Ignore empty directories when snapshotting template dir.
+ 3.0.3 - Avoid usage of deprecated APIs for newer versions of Gradle.
+ 3.0.2 - Incremental task functionality turned off when Rocker's hot reload feature is enabled.
+ 3.0.1 - Made RockerCompile task remove empty output directories.
+ 3.0 - Changed the DSL.
+ 2.2.1 - Improved support for configuration avoidance.
+ 2.2 - Made RockerCompile task support configuration avoidance.
+ 2.1.1 - Internal refactoring to improve lazy properties usage.
+ 2.1 - Internal refactoring to get rid of deprecated APIs that will be removed in Gradle 7.
+ 2.0 - Made RockerCompile task compatible with the upcoming Gradle Configuration Cache.
+ 1.0.1 - Made RockerCompile task compatible with Gradle's upcoming Instant Execution.
+ 1.0 - Made Gradle 5.0 the minimum compatible version and replaced usage of deprecated APIs.
+ 0.4 - Removed wiring between `clean` task and deleting generated Rocker sources, uses Rocker 1.2.0 by default.
+ 0.3.1 - Fixed incremental template compilation.
+ 0.3 - Incremental template compilation, i.e. only the modified templates are compiled.
+ 0.2 - New DSL, more Rocker configuration options, support for the Gradle Build Cache.
+ 0.1 - Initial version

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

+ [gregopet](https://github.com/gregopet) (detailed issue report #11)
+ [facewindu](https://github.com/facewindu) (pr #10 to remove empty output directories)
+ [wolfs](https://github.com/wolfs) (support to make lazy properties work)
+ [eskatos](https://github.com/eskatos) (support to make configuration caching work)
+ [breskeby](https://github.com/breskeby) (pr #6 to avoid deprecation warnings)
+ [mark-vieira](https://github.com/mark-vieira) (pr #2 and pr #3 to make RockerCompile task itself incremental)
+ [mark-vieira](https://github.com/mark-vieira) (pr #55 to the fizzed/rocker project to fix the MODIFIED_AT issue)
+ [ldaley](https://github.com/ldaley) (design discussions)

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
