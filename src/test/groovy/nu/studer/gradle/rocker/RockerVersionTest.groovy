package nu.studer.gradle.rocker

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RockerVersionTest extends Specification {

    def "parses version string #versionString"() {
        when:
        def version = RockerVersion.from(versionString)

        then:
        version.asString() == versionString

        where:
        versionString << ['1', '0.16', '0.16.0', '0.16.0-SNAPSHOT', 'SOME']
    }

}
