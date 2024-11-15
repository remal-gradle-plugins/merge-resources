package name.remal.gradle_plugins.merge_resources.mergers;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.Collection;

public abstract class SpringImportsMerger extends MetaInfServicesMerger {

    @Override
    public Collection<String> getIncludes() {
        return singletonList("META-INF/spring/*.imports");
    }

    @Override
    public Collection<String> getExcludes() {
        return emptyList();
    }

}
