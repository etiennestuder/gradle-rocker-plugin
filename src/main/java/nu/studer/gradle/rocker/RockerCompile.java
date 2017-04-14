package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@ParallelizableTask
@CacheableTask
public class RockerCompile extends DefaultTask {

    private RockerConfig config;
    private FileCollection runtimeClasspath;
    private Action<? super JavaExecSpec> javaExecSpec;
    private Action<? super ExecResult> execResultHandler;

    @SuppressWarnings("unused")
    @Nested
    public RockerConfig getConfig() {
        return config;
    }

    void setConfig(RockerConfig config) {
        this.config = config;
    }

    @SuppressWarnings("unused")
    @Classpath
    @InputFiles
    public FileCollection getRuntimeClasspath() {
        return runtimeClasspath;
    }

    void setRuntimeClasspath(FileCollection runtimeClasspath) {
        this.runtimeClasspath = runtimeClasspath;
    }

    @SuppressWarnings("unused")
    @Internal
    Action<? super JavaExecSpec> getJavaExecSpec() {
        return javaExecSpec;
    }

    @SuppressWarnings("unused")
    public void setJavaExecSpec(Action<? super JavaExecSpec> javaExecSpec) {
        this.javaExecSpec = javaExecSpec;
    }

    @SuppressWarnings("unused")
    @Internal
    Action<? super ExecResult> getExecResultHandler() {
        return execResultHandler;
    }

    @SuppressWarnings("unused")
    public void setExecResultHandler(Action<? super ExecResult> execResultHandler) {
        this.execResultHandler = execResultHandler;
    }

    @SuppressWarnings("unused")
    @TaskAction
    void doCompile() throws IOException {
        // delete any generated files from previous runs
        getProject().delete(config.getOutputDir());

        // generate the files from the templates
        ExecResult execResult = executeRocker();

        // for the Gradle Build Cache to function properly, the same inputs must always create exactly the same output
        // thus, if hot-reloading is disabled and the generated source code contains timestamps, we remove the MODIFIED_AT line
        RockerVersion rockerVersion = RockerVersion.fromProject(getProject());
        if (config.isOptimize() && rockerVersion.generatesRedundantCode_MODIFIED_AT()) {
            trimLine_MODIFIED_AT();
        }

        // invoke custom result handler
        if (execResultHandler != null) {
            execResultHandler.execute(execResult);
        }
    }

    private ExecResult executeRocker() {
        return getProject().javaexec(new Action<JavaExecSpec>() {

            @Override
            public void execute(JavaExecSpec spec) {
                spec.setMain("com.fizzed.rocker.compiler.JavaGeneratorMain");
                spec.setClasspath(runtimeClasspath);
                spec.systemProperty("rocker.option.optimize", Boolean.toString(config.isOptimize()));
                spec.systemProperty("rocker.option.targetCharset", config.getTargetCharset());
                spec.systemProperty("rocker.template.dir", config.getTemplateDir().getAbsolutePath());
                spec.systemProperty("rocker.output.dir", config.getOutputDir().getAbsolutePath());

                if (javaExecSpec != null) {
                    javaExecSpec.execute(spec);
                }
            }

        });
    }

    private void trimLine_MODIFIED_AT() throws IOException {
        Set<File> generatedFiles = getProject().fileTree(config.getOutputDir(), new Action<ConfigurableFileTree>() {
            @Override
            public void execute(ConfigurableFileTree tree) {
                tree.include("**/*.java");
            }
        }).getFiles();
        for (File file : generatedFiles) {
            Path path = file.toPath();
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("static public final long MODIFIED_AT = \\d+L;", "");
            Files.write(path, content.getBytes(charset));
        }
    }

}
