package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;

public class RockerConfig {

    @Internal
    public final String name;

    @Internal
    public final Project project;

    @Internal
    public Action<? super JavaExecSpec> javaExecSpec;

    @Internal
    public Action<? super ExecResult> execResultHandler;

    @Input
    public boolean optimize;

    @InputDirectory
    public File templateDir;

    private  File outputDir;

    public RockerConfig(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;

        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        SourceSet sourceSet = sourceSets.findByName(name);
        if (sourceSet != null) {
            sourceSet.getJava().srcDir(outputDir);
        }
    }

    @Override
    public String toString() {
        return "RockerConfig{" +
            "name='" + name + '\'' +
            ", javaExecSpec=" + javaExecSpec +
            ", execResultHandler=" + execResultHandler +
            ", optimize=" + optimize +
            ", templateDir=" + templateDir +
            ", outputDir=" + outputDir +
            '}';
    }

}
