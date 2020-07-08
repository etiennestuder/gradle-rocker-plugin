package nu.studer.gradle.rocker;

import org.gradle.api.Project;

import static java.util.Objects.requireNonNull;

final class RockerVersionProperty {

    private static final String PROJECT_PROPERTY = "rockerVersion";
    private static final String DEFAULT = "1.3.0";

    final String version;

    private RockerVersionProperty(String version) {
        this.version = requireNonNull(version);
    }

    static void applyDefaultVersion(Project project) {
        project.getExtensions().getExtraProperties().set(PROJECT_PROPERTY, DEFAULT);
    }

    static RockerVersionProperty fromProject(Project project) {
        return from((String) project.getExtensions().getExtraProperties().get(PROJECT_PROPERTY));
    }

    private static RockerVersionProperty from(String version) {
        return new RockerVersionProperty(version);
    }

    String asVersion() {
        return version;
    }

}
