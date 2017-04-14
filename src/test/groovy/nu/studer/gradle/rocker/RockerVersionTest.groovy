package nu.studer.gradle.rocker

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RockerVersionTest extends Specification {

    def "parses version string and formats back to version string #versionString "() {
        when:
        def version = RockerVersion.from(versionString)

        then:
        version.asString() == versionString

        where:
        versionString << ['1', '0.16', '0.16.0', '0.16.0-SNAPSHOT', 'SOME']
    }

    def "can check if rocker of given version adds MODIFIED_AT in generated source code"() {
        expect:
        version.generatesRedundantCode_MODIFIED_AT() == generatesMODIFIED_AT

        where:
        version                      | generatesMODIFIED_AT
        RockerVersion.from('0.17')   | true
        RockerVersion.from('0.17.0') | true
        RockerVersion.from('0.17.1') | true
        RockerVersion.from('0.18')   | false
        RockerVersion.from('0.18.0') | false
        RockerVersion.from('0.18.1') | false
    }

}
