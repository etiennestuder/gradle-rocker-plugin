package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static nu.studer.gradle.rocker.FileUtils.relativePath;

public class RockerCompile extends DefaultTask {

    private static final String ROCKER_FILE_EXTENSION_PREFIX = ".rocker";

    private RockerConfig config;
    private FileCollection runtimeClasspath;
    private Action<? super JavaExecSpec> javaExecSpec;
    private Action<? super ExecResult> execResultHandler;

    private final ObjectFactory objects;
    private final ProjectLayout projectLayout;
    private final FileSystemOperations fileSystemOperations;
    private final ExecOperations execOperations;

    // todo use new incremental task API https://docs.gradle.org/current/javadoc/org/gradle/work/InputChanges.html
    @Inject
    public RockerCompile(ObjectFactory objects, ProjectLayout projectLayout, FileSystemOperations fileSystemOperations, ExecOperations execOperations) {
        this.objects = objects;
        this.projectLayout = projectLayout;
        this.fileSystemOperations = fileSystemOperations;
        this.execOperations = execOperations;

        // do not use lambda due to a bug in Gradle 6.5
        getOutputs().cacheIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                return config.isOptimize();
            }
        });
    }

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
    void doCompile(IncrementalTaskInputs incrementalTaskInputs) {
        ExecResult execResult = null;
        final Set<File> modifiedTemplates = new HashSet<>();
        final Set<File> removedTemplates = new HashSet<>();

        if (!incrementalTaskInputs.isIncremental()) {
            // delete any generated files from previous runs and any classes compiled by Rocker via hot-reloading
            fileSystemOperations.delete(spec -> spec.delete(config.getOutputDir()));
            fileSystemOperations.delete(spec -> spec.delete(config.getClassDir()));

            // generate the files from the templates
            execResult = executeRocker(config.getTemplateDir());
        } else {
            // collect new/modified templates
            incrementalTaskInputs.outOfDate(fileDetails -> modifiedTemplates.add(fileDetails.getFile()));

            // collect stale files for removed templates
            incrementalTaskInputs.removed(fileDetails -> {
                String javaSourceFileName = toJavaSourceFileName(relativePath(config.getTemplateDir(), fileDetails.getFile()));
                if (javaSourceFileName != null) {
                    ConfigurableFileTree removedFile = objects.fileTree().from(config.getOutputDir());
                    removedFile.include(javaSourceFileName);
                    removedTemplates.addAll(removedFile.getFiles());
                }

                String javaClassFileName = toJavaClassFileName(relativePath(config.getTemplateDir(), fileDetails.getFile()));
                if (javaClassFileName != null) {
                    ConfigurableFileTree removedFile = objects.fileTree().from(config.getClassDir());
                    removedFile.include(javaClassFileName);
                    removedTemplates.addAll(removedFile.getFiles());
                }
            });

            // copy new/modified templates to a temporary folder before compiling them (to avoid recompilation of all templates)
            if (!modifiedTemplates.isEmpty()) {
                // copy modified files to a temp directory since we can only point Rocker to a directory
                final File tempDir = getTemporaryDir();
                fileSystemOperations.delete(spec -> spec.delete(tempDir));

                fileSystemOperations.copy(spec -> {
                    spec.from(config.getTemplateDir());
                    for (File template : modifiedTemplates) {
                        spec.include(relativePath(config.getTemplateDir(), template));
                    }
                    spec.into(tempDir);
                });

                // generate the files from the modified templates
                execResult = executeRocker(tempDir);
            }

            // remove the compiled files for any removed templates
            if (!removedTemplates.isEmpty()) {
                fileSystemOperations.delete(spec -> spec.delete(removedTemplates));
            }
        }

        // invoke custom result handler
        if (execResultHandler != null && execResult != null) {
            execResultHandler.execute(execResult);
        }
    }

    private ExecResult executeRocker(final File templateDir) {
        return execOperations.javaexec(new Action<JavaExecSpec>() {

            @Override
            public void execute(JavaExecSpec spec) {
                spec.setMain("com.fizzed.rocker.compiler.JavaGeneratorMain");
                spec.setClasspath(runtimeClasspath);
                spec.setWorkingDir(projectLayout.getProjectDirectory());
                spec.systemProperty("rocker.option.optimize", Boolean.toString(config.isOptimize()));
                systemPropertyIfNotNull("rocker.option.extendsClass", config.getExtendsClass(), spec);
                systemPropertyIfNotNull("rocker.option.extendsModelClass", config.getExtendsModelClass(), spec);
                systemPropertyIfNotNull("rocker.option.javaVersion", config.getJavaVersion(), spec);
                systemPropertyIfNotNull("rocker.option.targetCharset", config.getTargetCharset(), spec);
                spec.systemProperty("rocker.template.dir", templateDir.getAbsolutePath());
                spec.systemProperty("rocker.output.dir", config.getOutputDir().getAbsolutePath());
                spec.systemProperty("rocker.class.dir", config.getClassDir().getAbsolutePath());

                if (javaExecSpec != null) {
                    javaExecSpec.execute(spec);
                }
            }

            private void systemPropertyIfNotNull(String option, String value, JavaExecSpec spec) {
                if (value != null) {
                    spec.systemProperty(option, value);
                }
            }

        });
    }

    private static String toJavaSourceFileName(String templateName) {
        int extension = templateName.indexOf(ROCKER_FILE_EXTENSION_PREFIX);
        return extension > -1 ? templateName.substring(0, extension) + ".java" : null;
    }

    private static String toJavaClassFileName(String templateName) {
        int extension = templateName.indexOf(ROCKER_FILE_EXTENSION_PREFIX);
        return extension > -1 ? templateName.substring(0, extension) + ".class" : null;
    }

}
