package nu.studer.gradle.rocker;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;

import javax.inject.Inject;

public class RockerConfig {

    final String name;

    private final Property<Boolean> optimize;
    private final Property<String> extendsClass;
    private final Property<String> extendsModelClass;
    private final Property<String> javaVersion;
    private final Property<String> targetCharset;

    private final DirectoryProperty templateDir;
    private final DirectoryProperty outputDir;
    private final DirectoryProperty classDir;

    @Inject
    public RockerConfig(String name, ObjectFactory objects, ProjectLayout layout) {
        this.name = name;

        this.optimize = objects.property(Boolean.class).convention(Boolean.FALSE);
        this.extendsClass = objects.property(String.class).convention((String) null);
        this.extendsModelClass = objects.property(String.class).convention((String) null);
        this.javaVersion = objects.property(String.class).convention(Runtime.class.getPackage().getSpecificationVersion());
        this.targetCharset = objects.property(String.class).convention("UTF-8");

        this.templateDir = objects.directoryProperty().convention(layout.getProjectDirectory().dir("src/rocker/" + name));
        this.outputDir = objects.directoryProperty().convention(layout.getBuildDirectory().dir("generated-src/rocker/" + name));
        this.classDir = objects.directoryProperty().convention(layout.getBuildDirectory().dir("rocker-hot-reload/" + name));
    }

    @Input
    public Property<Boolean> getOptimize() {
        return optimize;
    }

    @Optional
    @Input
    public Property<String> getExtendsClass() {
        return extendsClass;
    }

    @Optional
    @Input
    public Property<String> getExtendsModelClass() {
        return extendsModelClass;
    }

    @Optional
    @Input
    public Property<String> getJavaVersion() {
        return javaVersion;
    }

    @Optional
    @Input
    public Property<String> getTargetCharset() {
        return targetCharset;
    }

    @SkipWhenEmpty
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getTemplateDir() {
        return templateDir;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    // do not include in uptodate check, not as input nor as output
    @Internal
    public DirectoryProperty getClassDir() {
        return classDir;
    }

    @Override
    public String toString() {
        return "RockerConfig{" +
            "name='" + name + '\'' +
            ", optimize=" + optimize +
            ", extendsClass='" + extendsClass + '\'' +
            ", extendsModelClass='" + extendsModelClass + '\'' +
            ", javaVersion='" + javaVersion + '\'' +
            ", targetCharset='" + targetCharset + '\'' +
            ", templateDir=" + templateDir +
            ", outputDir=" + outputDir +
            ", classDir=" + classDir +
            '}';
    }

}
