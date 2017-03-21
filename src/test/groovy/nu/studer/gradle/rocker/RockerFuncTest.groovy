package nu.studer.gradle.rocker

import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

@Unroll
class RockerFuncTest extends BaseFuncTest {

    void "runs task"() {
        given:
        buildFile << """
plugins {
    id 'nu.studer.rocker'
}
"""

        when:
        def result = runWithArguments('rocker', '-i')

        then:
        result.output.contains("rocker invoked")
        result.task(':rocker').outcome == TaskOutcome.SUCCESS
    }

}
