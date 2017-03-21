package nu.studer.gradle.rocker;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RockerTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RockerTask.class);

    @TaskAction
    public void make() {
        LOGGER.info("rocker invoked");
    }

}
