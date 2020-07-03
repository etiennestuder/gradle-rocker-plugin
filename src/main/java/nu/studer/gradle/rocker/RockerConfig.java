package nu.studer.gradle.rocker;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
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

    private boolean optimize;
    private String extendsClass;
    private String extendsModelClass;
    private String javaVersion;
    private String targetCharset;

    private final DirectoryProperty templateDir;
    private final DirectoryProperty outputDir;
    private final DirectoryProperty classDir;

    @Inject
    public RockerConfig(String name, Project project) {
        this.name = name;

        this.optimize = false;
        this.extendsClass = null;
        this.extendsModelClass = null;
        this.javaVersion = Runtime.class.getPackage().getSpecificationVersion();
        this.targetCharset = "UTF-8";

        ObjectFactory objects = project.getObjects();
        ProjectLayout layout = project.getLayout();

        this.templateDir = objects.directoryProperty().convention(layout.getProjectDirectory().dir("src/rocker/" + name));
        this.outputDir = objects.directoryProperty().convention(layout.getBuildDirectory().dir("generated-src/rocker/" + name));
        this.classDir = objects.directoryProperty().convention(layout.getBuildDirectory().dir("rocker-hot-reload/" + name));
    }

    @Input
    public boolean isOptimize() {
        return optimize;
    }

    @SuppressWarnings("unused")
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    @Optional
    @Input
    public String getExtendsClass() {
        return extendsClass;
    }

    @SuppressWarnings("unused")
    public void setExtendsClass(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    @Optional
    @Input
    public String getExtendsModelClass() {
        return extendsModelClass;
    }

    @SuppressWarnings("unused")
    public void setExtendsModelClass(String extendsModelClass) {
        this.extendsModelClass = extendsModelClass;
    }

    @Optional
    @Input
    public String getJavaVersion() {
        return javaVersion;
    }

    @SuppressWarnings("unused")
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    @Optional
    @Input
    public String getTargetCharset() {
        return targetCharset;
    }

    @SuppressWarnings("unused")
    public void setTargetCharset(String targetCharset) {
        this.targetCharset = targetCharset;
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
