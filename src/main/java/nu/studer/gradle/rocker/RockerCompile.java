package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

@ParallelizableTask
public class RockerCompile extends DefaultTask {

    @Nested
    public RockerConfig config;

    @TaskAction
    void doCompile() {
        getProject().delete(config.outputDir);
        ExecResult execResult = executeRocker();
        if (config.execResultHandler != null) {
            config.execResultHandler.execute(execResult);
        }
    }

    private ExecResult executeRocker() {
        return getProject().javaexec(new Action<JavaExecSpec>() {

            @Override
            public void execute(JavaExecSpec spec) {
                spec.setMain("com.fizzed.rocker.compiler.JavaGeneratorMain");
                spec.setClasspath(config.rockerCompiler);
                spec.systemProperty("rocker.optimize", Boolean.toString(config.optimize));
                spec.systemProperty("rocker.template.dir", config.templateDir.getAbsolutePath());
                spec.systemProperty("rocker.output.dir", config.outputDir.getAbsolutePath());
                spec.systemProperty("rocker.class.dir", config.outputDir.getAbsolutePath());
                if (config.javaExecSpec != null) {
                    config.javaExecSpec.execute(spec);
                }
            }

        });
    }

}
