package nu.studer.gradle.rocker;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class RockerPlugin implements Plugin<Project> {

    private static final String ROCKER_TASK_NAME = "rocker";

    private static final Logger LOGGER = LoggerFactory.getLogger(RockerPlugin.class);

    @Override
    public void apply(Project project) {
        // add rocker task
        RockerTask rocker = project.getTasks().create(ROCKER_TASK_NAME, RockerTask.class);
        rocker.setDescription("Invokes the Rocker template engine.");
        rocker.setGroup("Rocker");
    }

}
