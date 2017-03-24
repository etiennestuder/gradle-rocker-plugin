package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;

public class RockerConfig {

    @Internal
    public final String name;

    @Internal
    public Action<? super JavaExecSpec> javaExecSpec;

    @Internal
    public Action<? super ExecResult> execResultHandler;

    @InputFiles
    public FileCollection rockerCompiler;

    @Input
    public boolean optimize;

    @InputDirectory
    public File templateDir;

    @OutputDirectory
    public File outputDir;

    public RockerConfig(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RockerConfig{" +
            "name='" + name + '\'' +
            ", javaExecSpec=" + javaExecSpec +
            ", execResultHandler=" + execResultHandler +
            ", rockerCompiler=" + rockerCompiler +
            ", optimize=" + optimize +
            ", templateDir=" + templateDir +
            ", outputDir=" + outputDir +
            '}';
    }

}
