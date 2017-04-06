package nu.studer.gradle.rocker;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.Set;

public class RockerConfig {

    final String name;
    private final Project project;

    private boolean optimize;
    private File templateDir;
    private File outputDir;

    RockerConfig(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    @Input
    boolean isOptimize() {
        return optimize;
    }

    @SuppressWarnings("unused")
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    File getTemplateDir() {
        return templateDir;
    }

    @SuppressWarnings("unused")
    public void setTemplateDir(File templateDir) {
        this.templateDir = templateDir;
    }

    @OutputDirectory
    File getOutputDir() {
        return outputDir;
    }

    @SuppressWarnings("unused")
    public void setOutputDir(File outputDir) {
        File previousOutputDir = this.outputDir;
        this.outputDir = outputDir;

        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        SourceSet sourceSet = sourceSets.findByName(name);
        if (sourceSet != null) {
            Set<File> srcDirs = sourceSet.getJava().getSrcDirs();
            srcDirs.remove(previousOutputDir);
            srcDirs.add(outputDir);
            sourceSet.getJava().setSrcDirs(srcDirs);
        }
    }

    @Override
    public String toString() {
        return "RockerConfig{" +
            "name='" + name + '\'' +
            ", optimize=" + optimize +
            ", templateDir=" + templateDir +
            ", outputDir=" + outputDir +
            '}';
    }

}
