package nu.studer.gradle.rocker;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
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

import java.io.File;

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

    RockerConfig(String name, Project project) {
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
    boolean isOptimize() {
        return optimize;
    }

    @SuppressWarnings("unused")
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    @Optional
    @Input
    String getExtendsClass() {
        return extendsClass;
    }

    @SuppressWarnings("unused")
    public void setExtendsClass(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    @Optional
    @Input
    String getExtendsModelClass() {
        return extendsModelClass;
    }

    @SuppressWarnings("unused")
    public void setExtendsModelClass(String extendsModelClass) {
        this.extendsModelClass = extendsModelClass;
    }

    @Optional
    @Input
    String getJavaVersion() {
        return javaVersion;
    }

    @SuppressWarnings("unused")
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    @Optional
    @Input
    String getTargetCharset() {
        return targetCharset;
    }

    @SuppressWarnings("unused")
    public void setTargetCharset(String targetCharset) {
        this.targetCharset = targetCharset;
    }

    @SkipWhenEmpty
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    DirectoryProperty getTemplateDir() {
        return templateDir;
    }

    @SuppressWarnings("unused")
    public void setTemplateDir(File templateDir) {
        this.templateDir.set(templateDir);
    }

    @SuppressWarnings("unused")
    public void setTemplateDir(Directory templateDir) {
        this.templateDir.set(templateDir);
    }

    @OutputDirectory
    DirectoryProperty getOutputDir() {
        return outputDir;
    }

    @SuppressWarnings("unused")
    public void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir);
    }

    @SuppressWarnings("unused")
    public void setOutputDir(Directory outputDir) {
        this.outputDir.set(outputDir);
    }

    // do not include in uptodate check, not as input nor as output
    @Internal
    DirectoryProperty getClassDir() {
        return classDir;
    }

    @SuppressWarnings("unused")
    public void setClassDir(File classDir) {
        this.classDir.set(classDir);
    }

    @SuppressWarnings("unused")
    public void setClassDir(Directory classDir) {
        this.classDir.set(classDir);
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
