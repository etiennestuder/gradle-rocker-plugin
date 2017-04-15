package nu.studer.gradle.rocker;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.Set;

public class RockerConfig {

    final String name;
    private final Project project;

    private boolean optimize;
    private String extendsClass;
    private String extendsModelClass;
    private String javaVersion;
    private String targetCharset;

    private File templateDir;
    private File outputDir;
    private File classDir;

    RockerConfig(String name, Project project) {
        this.name = name;
        this.project = project;

        this.optimize = false;
        this.extendsClass = null;
        this.extendsModelClass = null;
        this.javaVersion = Runtime.class.getPackage().getSpecificationVersion();
        this.targetCharset = "UTF-8";

        this.templateDir = new File(project.getProjectDir(), "src/rocker/" + name);
        this.outputDir = new File(project.getBuildDir(), "generated-src/rocker/" + name);
        this.classDir = new File(project.getBuildDir(), "rocker-hot-reload/" + name);
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

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    File getTemplateDir() {
        return templateDir;
    }

    @SuppressWarnings("unused")
    public void setTemplateDir(File templateDir) {
        this.templateDir = templateDir;
    }

    @OutputDirectory
    File getOutputDir() {
        return outputDir;
    }

    @SuppressWarnings("unused")
    public void setOutputDir(File outputDir) {
        File previousOutputDir = this.outputDir;
        this.outputDir = outputDir;

        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        SourceSet sourceSet = sourceSets.findByName(name);
        if (sourceSet != null) {
            Set<File> srcDirs = sourceSet.getJava().getSrcDirs();
            srcDirs.remove(previousOutputDir);
            srcDirs.add(outputDir);
            sourceSet.getJava().setSrcDirs(srcDirs);
        }
    }

    // do not annotate as @OutputDirectory!
    @Input
    File getClassDir() {
        return classDir;
    }

    @SuppressWarnings("unused")
    public void setClassDir(File classDir) {
        this.classDir = classDir;
    }

    @Override
    public String toString() {
        return "RockerConfig{" +
            "name='" + name + '\'' +
            ", project=" + project +
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
