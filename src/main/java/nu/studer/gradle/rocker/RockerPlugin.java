package nu.studer.gradle.rocker;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static nu.studer.gradle.rocker.GradleUtils.isAtLeastGradleVersion;
import static nu.studer.gradle.rocker.StringUtils.capitalize;

@SuppressWarnings("unused")
public class RockerPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RockerPlugin.class);

    @Override
    public void apply(Project project) {
        // abort if old Gradle version is not supported
        if (GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version("6.1")) < 0) {
            throw new IllegalStateException("This version of the rocker plugin is not compatible with Gradle < 6.1");
        }

        // apply Java base plugin, making it possible to also use the rocker plugin for Android builds
        project.getPlugins().apply(JavaBasePlugin.class);

        // add Rocker DSL extension
        RockerExtension rockerExtension = project.getExtensions().create("rocker", RockerExtension.class);

        // create configuration for the runtime classpath of the rocker compiler (shared by all rocker configuration domain objects)
        final Configuration runtimeConfiguration = createRockerCompilerRuntimeConfiguration(project);

        // create a rocker task for each rocker configuration domain object
        rockerExtension.getConfigurations().configureEach(config -> {
            // register rocker task, create it lazily
            String taskName = "compile" + (config.name.equals("main") ? "" : capitalize(config.name)) + "Rocker";
            TaskProvider<RockerCompile> rocker = project.getTasks().register(taskName, RockerCompile.class, config, runtimeConfiguration);
            rocker.configure(task -> {
                task.setDescription(format("Compiles the Rocker templates of the %s rocker configuration.", config.name));
                task.setGroup("Rocker");
            });

            // add the output of the rocker task as a source directory of the source set with the matching name (which adds an implicit task dependency)
            // add the rocker-runtime to the compile configuration in order to be able to compile the generated sources
            SourceSetContainer sourceSets;
            if (isAtLeastGradleVersion("7.1")) {
                sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
            } else {
                sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
            }
            sourceSets.configureEach(sourceSet -> {
                if (sourceSet.getName().equals(config.name)) {
                    sourceSet.getJava().srcDir(rocker.flatMap(RockerCompile::getOutputDir));
                    project.getDependencies().add(sourceSet.getImplementationConfigurationName(), "com.fizzed:rocker-runtime");
                }
            });
        });

        // use the configured rocker version on all rocker dependencies
        enforceRockerVersion(project, rockerExtension);
    }

    private static Configuration createRockerCompilerRuntimeConfiguration(Project project) {
        Configuration rockerCompilerRuntime = project.getConfigurations().create("rockerCompiler");
        rockerCompilerRuntime.setDescription("The classpath used to invoke the Rocker template engine. Add your additional dependencies here.");
        project.getDependencies().add(rockerCompilerRuntime.getName(), "com.fizzed:rocker-compiler");
        project.getDependencies().add(rockerCompilerRuntime.getName(), "org.slf4j:slf4j-simple:1.7.30");
        return rockerCompilerRuntime;
    }

    private static void enforceRockerVersion(Project project, RockerExtension rockerExtension) {
        project.getConfigurations().configureEach(configuration ->
            configuration.getResolutionStrategy().eachDependency(details -> {
                ModuleVersionSelector requested = details.getRequested();
                if (requested.getGroup().equals("com.fizzed") && requested.getName().startsWith("rocker-")) {
                    String version = rockerExtension.getVersion().get();
                    details.useVersion(version);
                }
            })
        );
    }

}
