package nu.studer.gradle.rocker;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class RockerPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RockerPlugin.class);

    @Override
    public void apply(Project project) {
        // abort if old Gradle version is not supported
        if (GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version("5.0")) < 0) {
            throw new IllegalStateException("This version of the rocker plugin is not compatible with Gradle < 5.0");
        }

        // apply Java base plugin, making it possible to also use the rocker plugin for Android builds
        project.getPlugins().apply(JavaBasePlugin.class);

        // allow to configure the rocker version via extension property
        RockerVersion.applyDefaultVersion(project);

        // use the configured rocker version on all rocker dependencies
        enforceRockerVersion(project);

        // add rocker DSL extension
        NamedDomainObjectContainer<RockerConfig> container = project.container(RockerConfig.class, name -> new RockerConfig(name, project));
        project.getExtensions().add("rocker", container);

        // create configuration for the runtime classpath of the rocker compiler (shared by all rocker configuration domain objects)
        final Configuration configuration = createRockerCompilerRuntimeConfiguration(project);

        // create a rocker task for each rocker configuration domain object
        container.all(config -> {
            // create rocker task
            String taskName = "compile" + (config.name.equals("main") ? "" : StringUtils.capitalize(config.name)) + "Rocker";
            RockerCompile rocker = project.getTasks().create(taskName, RockerCompile.class);
            rocker.setDescription("Invokes the Rocker template engine.");
            rocker.setGroup("Rocker");
            rocker.setConfig(config);
            rocker.setRuntimeClasspath(configuration);

            // add the output of the rocker task as a source directory of the source set with the matching name (which adds an implicit task dependency)
            // add the rocker-runtime to the compile configuration in order to be able to compile the generated sources
            SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
            SourceSet sourceSet = sourceSets.findByName(config.name);
            if (sourceSet != null) {
                sourceSet.getJava().srcDir(rocker);
                project.getDependencies().add(sourceSet.getImplementationConfigurationName(), "com.fizzed:rocker-runtime");
            }
        });
    }

    private void enforceRockerVersion(Project project) {
        project.getConfigurations().all(configuration ->
            configuration.getResolutionStrategy().eachDependency(details -> {
                ModuleVersionSelector requested = details.getRequested();
                if (requested.getGroup().equals("com.fizzed") && requested.getName().startsWith("rocker-")) {
                    details.useVersion(RockerVersion.fromProject(project).asString());
                }
            })
        );
    }

    private Configuration createRockerCompilerRuntimeConfiguration(Project project) {
        Configuration rockerCompilerRuntime = project.getConfigurations().create("rockerCompiler");
        rockerCompilerRuntime.setDescription("The classpath used to invoke the Rocker template engine. Add your additional dependencies here.");
        project.getDependencies().add(rockerCompilerRuntime.getName(), "com.fizzed:rocker-compiler");
        project.getDependencies().add(rockerCompilerRuntime.getName(), "org.slf4j:slf4j-simple:1.7.30");
        return rockerCompilerRuntime;
    }

}
