package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

@ParallelizableTask
@CacheableTask
public class RockerCompile extends DefaultTask {

    private RockerConfig config;
    private FileCollection runtimeClasspath;

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
    @TaskAction
    void doCompile() {
        getProject().delete(config.getOutputDir());
        ExecResult execResult = executeRocker();

        Action<? super ExecResult> resultHandler = config.getExecResultHandler();
        if (resultHandler != null) {
            resultHandler.execute(execResult);
        }
    }

    private ExecResult executeRocker() {
        return getProject().javaexec(new Action<JavaExecSpec>() {

            @Override
            public void execute(JavaExecSpec spec) {
                spec.setMain("com.fizzed.rocker.compiler.JavaGeneratorMain");
                spec.setClasspath(runtimeClasspath);
                spec.systemProperty("rocker.optimize", Boolean.toString(config.isOptimize()));
                spec.systemProperty("rocker.template.dir", config.getTemplateDir().getAbsolutePath());
                spec.systemProperty("rocker.output.dir", config.getOutputDir().getAbsolutePath());

                Action<? super JavaExecSpec> execSpec = config.getJavaExecSpec();
                if (execSpec != null) {
                    execSpec.execute(spec);
                }
            }

        });
    }

}
