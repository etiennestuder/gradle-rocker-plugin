package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileType;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import org.gradle.work.ChangeType;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static nu.studer.gradle.rocker.GradleUtils.isAtLeastGradleVersion;

public class RockerCompile extends DefaultTask {

    private static final String ROCKER_FILE_EXTENSION_PREFIX = ".rocker";

    private final Provider<Boolean> optimize;
    private final Provider<String> extendsClass;
    private final Provider<String> extendsModelClass;
    private final Provider<String> javaVersion;
    private final Provider<String> targetCharset;
    private final Provider<Directory> templateDir;
    private final FileCollection runtimeClasspath;
    private final Provider<Directory> outputDir;
    private final Provider<Directory> classDir;
    private Action<? super JavaExecSpec> javaExecSpec;
    private Action<? super ExecResult> execResultHandler;

    private final ProjectLayout projectLayout;
    private final FileSystemOperations fileSystemOperations;
    private final ExecOperations execOperations;

    @Inject
    public RockerCompile(RockerConfig config, FileCollection runtimeClasspath, ObjectFactory objects, ProjectLayout projectLayout, FileSystemOperations fileSystemOperations, ExecOperations execOperations) {
        this.optimize = objects.property(Boolean.class).value(config.getOptimize());
        this.extendsClass = objects.property(String.class).value(config.getExtendsClass());
        this.extendsModelClass = objects.property(String.class).value(config.getExtendsModelClass());
        this.javaVersion = objects.property(String.class).value(config.getJavaVersion());
        this.targetCharset = objects.property(String.class).value(config.getTargetCharset());
        this.templateDir = objects.directoryProperty().value(config.getTemplateDir());
        this.runtimeClasspath = objects.fileCollection().from(runtimeClasspath);
        this.outputDir = objects.directoryProperty().value(config.getOutputDir());
        this.classDir = objects.directoryProperty().value(config.getClassDir());

        this.projectLayout = projectLayout;
        this.fileSystemOperations = fileSystemOperations;
        this.execOperations = execOperations;

        // do not use lambda due to a bug in Gradle 6.5
        getOutputs().cacheIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                return optimize.get();
            }
        });
    }

    @Input
    public Provider<Boolean> getOptimize() {
        return optimize;
    }

    @Optional
    @Input
    public Provider<String> getExtendsClass() {
        return extendsClass;
    }

    @Optional
    @Input
    public Provider<String> getExtendsModelClass() {
        return extendsModelClass;
    }

    @Optional
    @Input
    public Provider<String> getJavaVersion() {
        return javaVersion;
    }

    @Optional
    @Input
    public Provider<String> getTargetCharset() {
        return targetCharset;
    }

    @SkipWhenEmpty
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public Provider<Directory> getTemplateDir() {
        return templateDir;
    }

    @SuppressWarnings("unused")
    @Classpath
    public FileCollection getRuntimeClasspath() {
        return runtimeClasspath;
    }

    @OutputDirectory
    public Provider<Directory> getOutputDir() {
        return outputDir;
    }

    @Internal
    public Provider<Directory> getClassDir() {
        return classDir;
    }

    @SuppressWarnings("unused")
    @Internal
    public Action<? super JavaExecSpec> getJavaExecSpec() {
        return javaExecSpec;
    }

    @SuppressWarnings("unused")
    public void setJavaExecSpec(Action<? super JavaExecSpec> javaExecSpec) {
        this.javaExecSpec = javaExecSpec;
    }

    @SuppressWarnings("unused")
    @Internal
    public Action<? super ExecResult> getExecResultHandler() {
        return execResultHandler;
    }

    @SuppressWarnings("unused")
    public void setExecResultHandler(Action<? super ExecResult> execResultHandler) {
        this.execResultHandler = execResultHandler;
    }

    @SuppressWarnings("unused")
    @TaskAction
    void doCompile(InputChanges inputChanges) {
        final File templateDirAsFile = templateDir.get().getAsFile();

        final Set<String> modifiedTemplates = new HashSet<>();
        final Set<File> removedTemplates = new HashSet<>();
        ExecResult execResult = null;

        if (!inputChanges.isIncremental() || !optimize.get()) {
            // delete any generated files from previous runs and any classes compiled by Rocker via hot-reloading
            fileSystemOperations.delete(spec -> spec.delete(outputDir));
            fileSystemOperations.delete(spec -> spec.delete(classDir));

            // generate the files from the templates
            execResult = executeRocker(templateDirAsFile);
        } else {
            inputChanges.getFileChanges(templateDir).forEach(change -> {
                if (change.getFileType() == FileType.DIRECTORY) {
                    return;
                }

                if (change.getChangeType() == ChangeType.ADDED || change.getChangeType() == ChangeType.MODIFIED) {
                    modifiedTemplates.add(change.getNormalizedPath());
                } else if (change.getChangeType() == ChangeType.REMOVED) {
                    String javaSourceFileName = toJavaSourceFileName(change.getNormalizedPath());
                    if (javaSourceFileName != null) {
                        removedTemplates.add(outputDir.get().file(javaSourceFileName).getAsFile());
                    }

                    String javaClassFileName = toJavaClassFileName(change.getNormalizedPath());
                    if (javaClassFileName != null) {
                        removedTemplates.add(classDir.get().file(javaClassFileName).getAsFile());
                    }
                }
            });

            // copy new/modified templates to a temporary folder before compiling them (to avoid recompilation of all templates)
            if (!modifiedTemplates.isEmpty()) {
                // copy modified files to a temp directory since we can only point Rocker to a directory
                final File tempDir = getTemporaryDir();
                fileSystemOperations.delete(spec -> spec.delete(tempDir));

                fileSystemOperations.copy(spec -> {
                    spec.from(templateDir);
                    for (String template : modifiedTemplates) {
                        spec.include(template);
                    }
                    spec.into(tempDir);
                });

                // generate the files from the modified templates
                execResult = executeRocker(tempDir);
            }

            // remove the compiled files for any removed templates (and remove all empty directories)
            if (!removedTemplates.isEmpty()) {
                fileSystemOperations.delete(spec -> spec.delete(removedTemplates));
                deleteEmptyDirectories(outputDir.get().getAsFile());
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
                setMainClass("com.fizzed.rocker.compiler.JavaGeneratorMain", spec);
                spec.setClasspath(runtimeClasspath);
                spec.setWorkingDir(projectLayout.getProjectDirectory());
                spec.systemProperty("rocker.option.optimize", optimize.get().toString());
                systemPropertyIfNotNull("rocker.option.extendsClass", extendsClass.getOrNull(), spec);
                systemPropertyIfNotNull("rocker.option.extendsModelClass", extendsModelClass.getOrNull(), spec);
                systemPropertyIfNotNull("rocker.option.javaVersion", javaVersion.getOrNull(), spec);
                systemPropertyIfNotNull("rocker.option.targetCharset", targetCharset.getOrNull(), spec);
                spec.systemProperty("rocker.template.dir", templateDir.getAbsolutePath());
                spec.systemProperty("rocker.output.dir", outputDir.get().getAsFile().getAbsolutePath());
                spec.systemProperty("rocker.class.dir", classDir.get().getAsFile().getAbsolutePath());

                if (javaExecSpec != null) {
                    javaExecSpec.execute(spec);
                }
            }

            private void setMainClass(String mainClass, JavaExecSpec spec) {
                if (isAtLeastGradleVersion("6.4")) {
                    spec.getMainClass().set(mainClass);
                } else {
                    setMainClassDeprecated(mainClass, spec);
                }
            }

            @SuppressWarnings("deprecation")
            private void setMainClassDeprecated(String mainClass, JavaExecSpec spec) {
                spec.setMain(mainClass);
            }

            private void systemPropertyIfNotNull(String option, String value, JavaExecSpec spec) {
                if (value != null) {
                    spec.systemProperty(option, value);
                }
            }

        });
    }

    private void deleteEmptyDirectories(File dir) {
        Arrays.stream(dir.listFiles(File::isDirectory)).forEach(this::deleteEmptyDirectories);
        if (dir.list().length == 0) {
            fileSystemOperations.delete(spec -> spec.delete(dir));
        }
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
