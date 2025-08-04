package nu.studer.gradle.rocker;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class RockerExtension {

    static final String DEFAULT_VERSION = "2.2.1";

    private final Property<String> version;
    private final NamedDomainObjectContainer<RockerConfig> configurations;

    @Inject
    public RockerExtension(ObjectFactory objects) {
        this.version = objects.property(String.class).convention(DEFAULT_VERSION);
        this.configurations = objects.domainObjectContainer(RockerConfig.class, name -> objects.newInstance(RockerConfig.class, name));

        version.finalizeValueOnRead();
    }

    @SuppressWarnings("unused")
    public Property<String> getVersion() {
        return version;
    }

    @SuppressWarnings("unused")
    public NamedDomainObjectContainer<RockerConfig> getConfigurations() {
        return configurations;
    }

}
