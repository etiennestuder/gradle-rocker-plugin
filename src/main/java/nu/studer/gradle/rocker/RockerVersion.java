package nu.studer.gradle.rocker;

import org.gradle.api.Project;

final class RockerVersion {

    private static final String PROJECT_PROPERTY = "rockerVersion";
    private static final String DEFAULT = "0.16.0";

    private final String major;
    private final String minor;
    private final String patch;

    private RockerVersion(String major, String minor, String patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    String asString() {
        StringBuilder string = new StringBuilder(major);
        if (minor != null) {
            string.append(".").append(minor);
        }
        if (patch != null) {
            string.append(".").append(patch);
        }
        return string.toString();
    }

    static void applyDefaultVersion(Project project) {
        project.getExtensions().getExtraProperties().set(PROJECT_PROPERTY, DEFAULT);
    }

    static RockerVersion fromProject(Project project) {
        return from(project.getExtensions().getExtraProperties().get(PROJECT_PROPERTY).toString());
    }

    static RockerVersion from(String versionString) {
        String[] majorMinorPatch = versionString.split("\\.", 3);
        return new RockerVersion(majorMinorPatch[0], majorMinorPatch.length > 1 ? majorMinorPatch[1] : null, majorMinorPatch.length > 2 ? majorMinorPatch[2] : null);
    }

}
