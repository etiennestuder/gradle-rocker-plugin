package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;

@ParallelizableTask
public class RockerCompile extends DefaultTask {

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

    @TaskAction
    void doCompile() {
        getProject().delete(outputDir);
        ExecResult execResult = executeRocker();
        if (execResultHandler != null) {
            execResultHandler.execute(execResult);
        }
    }

    private ExecResult executeRocker() {
        return getProject().javaexec(new Action<JavaExecSpec>() {

            @Override
            public void execute(JavaExecSpec spec) {
                spec.setMain("com.fizzed.rocker.compiler.JavaGeneratorMain");
                spec.setClasspath(rockerCompiler);
                spec.systemProperty("rocker.option.optimize", Boolean.toString(optimize));
                spec.systemProperty("rocker.template.dir", templateDir.getAbsolutePath());
                spec.systemProperty("rocker.output.dir", outputDir.getAbsolutePath());
                spec.systemProperty("rocker.class.dir", outputDir.getAbsolutePath());
                if (javaExecSpec != null) {
                    javaExecSpec.execute(spec);
                }
            }

        });
    }

}
