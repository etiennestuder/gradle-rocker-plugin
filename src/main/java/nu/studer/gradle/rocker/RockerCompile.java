package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static nu.studer.gradle.rocker.FileUtils.*;

@ParallelizableTask
public class RockerCompile extends DefaultTask {

    private static final String ROCKER_FILE_EXTENSION_PREFIX = ".rocker";

    private RockerConfig config;
    private FileCollection runtimeClasspath;
    private Action<? super JavaExecSpec> javaExecSpec;
    private Action<? super ExecResult> execResultHandler;

    public RockerCompile() {
        getOutputs().cacheIf(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task task) {
                return config.isOptimize();
            }
        });
    }

    private static String toJavaSourceFileName(String templateName) {
        int extension = templateName.indexOf(ROCKER_FILE_EXTENSION_PREFIX);
        return extension > -1 ? templateName.substring(0, extension) + ".java" : null;
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
    void doCompile(IncrementalTaskInputs incrementalTaskInputs) throws IOException {
        ExecResult execResult = null;
        final Set<File> modifiedTemplates = new HashSet<>();

        if (!incrementalTaskInputs.isIncremental()) {
            // delete any generated files from previous runs and any classes compiled by Rocker via hot-reloading
            getProject().delete(config.getOutputDir());
            getProject().delete(config.getClassDir());

            // generate the files from the templates
            execResult = executeRocker(config.getTemplateDir());
        } else {
            // collect modified files
            incrementalTaskInputs.outOfDate(new Action<InputFileDetails>() {
                @Override
                public void execute(InputFileDetails fileDetails) {
                    modifiedTemplates.add(fileDetails.getFile());
                }
            });

            // clean any stale files
            incrementalTaskInputs.removed(new Action<InputFileDetails>() {
                @Override
                public void execute(final InputFileDetails fileDetails) {
                    FileTree staleFiles = getProject().fileTree(config.getOutputDir(), new Action<ConfigurableFileTree>() {
                        @Override
                        public void execute(ConfigurableFileTree files) {
                            String javaSourceFileName = toJavaSourceFileName(relativePath(config.getTemplateDir(), fileDetails.getFile()));
                            if (javaSourceFileName != null) {
                                files.include(javaSourceFileName);
                            }
                        }
                    });
                    getProject().delete(staleFiles);

                    // todo (etst) should we also remove them from the classDir ?
                }
            });

            // copy new/modified templates to a temporary folder
            if (!modifiedTemplates.isEmpty()) {
                // copy modified files to a temp directory since we can only point Rocker to a directory
                final File tempDir = getTemporaryDir();

                getProject().copy(new Action<CopySpec>() {
                    @Override
                    public void execute(CopySpec spec) {
                        spec.from(config.getTemplateDir());
                        for (File template : modifiedTemplates) {
                            spec.include(relativePath(config.getTemplateDir(), template));
                        }
                        spec.into(tempDir);
                    }
                });

                // generate the files from the modified templates
                execResult = executeRocker(tempDir);
            }
        }

        // for the Gradle Build Cache to function properly, the same inputs must always create exactly the same output
        // thus, if hot-reloading is disabled and the generated source code contains timestamps, we remove the MODIFIED_AT line
        RockerVersion rockerVersion = RockerVersion.fromProject(getProject());
        if (config.isOptimize() && rockerVersion.generatesRedundantCode_MODIFIED_AT()) {
            trimLine_MODIFIED_AT(modifiedTemplates);
        }

        // invoke custom result handler
        if (execResultHandler != null && execResult != null) {
            execResultHandler.execute(execResult);
        }
    }

    private ExecResult executeRocker(final File templateDir) {
        return getProject().javaexec(new Action<JavaExecSpec>() {

            @Override
            public void execute(JavaExecSpec spec) {
                spec.setMain("com.fizzed.rocker.compiler.JavaGeneratorMain");
                spec.setClasspath(runtimeClasspath);
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

    private void trimLine_MODIFIED_AT(final Collection<File> updated) throws IOException {
        Charset charset = Charset.forName(config.getTargetCharset());
        Set<File> generatedFiles = getProject().fileTree(config.getOutputDir(), new Action<ConfigurableFileTree>() {
            @Override
            public void execute(ConfigurableFileTree tree) {
                if (updated.isEmpty()) {
                    tree.include("**/*.java");
                } else {
                    for (File file : updated) {
                        tree.include(relativePath(config.getOutputDir(), file));
                    }
                }
            }
        }).getFiles();
        for (File file : generatedFiles) {
            Path path = file.toPath();
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("static public final long MODIFIED_AT = \\d+L;", "");
            Files.write(path, content.getBytes(charset));
        }
    }

}
