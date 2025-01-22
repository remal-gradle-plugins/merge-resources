package name.remal.gradle_plugins.merge_resources.mergers;

import java.util.Collection;
import java.util.List;

public abstract class SpringImportsMerger extends MetaInfServicesMerger {

    @Override
    public Collection<String> getIncludes() {
        return List.of("META-INF/spring/*.imports");
    }

    @Override
    public Collection<String> getExcludes() {
        return List.of();
    }

}
