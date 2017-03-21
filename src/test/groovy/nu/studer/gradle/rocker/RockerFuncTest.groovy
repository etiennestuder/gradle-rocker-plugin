package nu.studer.gradle.rocker

import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

@Unroll
class RockerFuncTest extends BaseFuncTest {

    void "runs task"() {
        given:
        file('src/rocker/Example.rocker.html') << """
@args (String message)
Hello @message!
"""

        buildFile << """
plugins {
    id 'nu.studer.rocker'
}

apply plugin: 'java'

repositories {
    jcenter()
}

configurations {
    rockerCompiler
}

dependencies {
    compile 'com.fizzed:rocker-runtime:0.16.0'
    rockerCompiler 'com.fizzed:rocker-compiler:0.16.0'
    rockerCompiler 'org.slf4j:slf4j-simple:1.7.23'
}

rocker {
    rockerCompiler = project.configurations.rockerCompiler
    templateDir = file('src/rocker')
    outputDir = file('src/generated/rocker')
}
"""

        when:
        def result = runWithArguments('rocker')

        then:
        file('src/generated/rocker/Example.java').exists()
        result.output.contains("Parsing 1 rocker template files")
        result.output.contains("Generated 1 rocker java source files")
        result.output.contains("Generated rocker configuration ")
        result.task(':rocker').outcome == TaskOutcome.SUCCESS
    }

}
