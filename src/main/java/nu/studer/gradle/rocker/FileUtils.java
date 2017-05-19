package nu.studer.gradle.rocker;

import java.io.File;

final class FileUtils {

    static String relativePath(File baseDir, File child) {
        return baseDir.toPath().relativize(child.toPath()).toString();
    }

    private FileUtils() {
    }

}
