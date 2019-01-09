package nu.studer.gradle.rocker;

import org.gradle.api.Project;

import java.util.Objects;

final class RockerVersion {

    private static final String PROJECT_PROPERTY = "rockerVersion";
    private static final String DEFAULT = "1.2.0";

    private final String versionString;

    private RockerVersion(String versionString) {
        this.versionString = versionString;
    }

    static void applyDefaultVersion(Project project) {
        project.getExtensions().getExtraProperties().set(PROJECT_PROPERTY, DEFAULT);
    }

    static RockerVersion fromProject(Project project) {
        return from(Objects.requireNonNull(project.getExtensions().getExtraProperties().get(PROJECT_PROPERTY)).toString());
    }

    static RockerVersion from(String versionString) {
        return new RockerVersion(versionString);
    }

    String asString() {
        return versionString;
    }

}
