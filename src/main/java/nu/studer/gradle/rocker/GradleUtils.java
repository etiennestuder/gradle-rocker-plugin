package nu.studer.gradle.rocker;

import org.gradle.util.GradleVersion;

final class GradleUtils {

    static boolean isAtLeastGradleVersion(String version) {
        return GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version(version)) >= 0;
    }

    private GradleUtils() {
    }

}
