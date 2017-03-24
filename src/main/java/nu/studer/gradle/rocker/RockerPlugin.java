package nu.studer.gradle.rocker;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaBasePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class RockerPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RockerPlugin.class);

    @Override
    public void apply(final Project project) {
        // apply Java base plugin, making it possible to also use the rocker plugin for Android builds
        project.getPlugins().apply(JavaBasePlugin.class);

        // add rocker DSL extension
        NamedDomainObjectContainer<RockerConfig> container = project.container(RockerConfig.class);
        project.getExtensions().add("rocker", container);

        // create configuration for the runtime classpath of the rocker compiler (shared by all rocker configuration domain objects)
        final Configuration configuration = createRockerCompilerRuntimeConfiguration(project);

        // create a rocker task for each rocker configuration domain object
        container.all(new Action<RockerConfig>() {
            @Override
            public void execute(RockerConfig config) {
                // create task
                RockerCompile rocker = project.getTasks().create("rocker" + capitalize(config.name), RockerCompile.class);
                rocker.setGroup("Rocker");
                rocker.setDescription("Invokes the Rocker template engine.");
                rocker.config = config;
                rocker.rockerCompilerRuntime = configuration;
            }
        });
    }

    private Configuration createRockerCompilerRuntimeConfiguration(Project project) {
        Configuration rockerCompilerRuntime = project.getConfigurations().create("rockerCompiler");
        rockerCompilerRuntime.setDescription("The classpath used to invoke the Rocker template engine. Add your additional dependencies here.");
        project.getDependencies().add(rockerCompilerRuntime.getName(), "com.fizzed:rocker-compiler:0.16.0");
        project.getDependencies().add(rockerCompilerRuntime.getName(), "org.slf4j:slf4j-simple:1.7.23");
        return rockerCompilerRuntime;
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

}
