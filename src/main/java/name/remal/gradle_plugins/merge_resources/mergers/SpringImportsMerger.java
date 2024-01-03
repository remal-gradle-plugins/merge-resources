package name.remal.gradle_plugins.merge_resources.mergers;

import static java.util.Collections.singletonList;

import java.util.Collection;

public abstract class SpringImportsMerger extends MetaInfServicesMerger {

    @Override
    protected Collection<String> getIncludes() {
        return singletonList("META-INF/spring/*.imports");
    }

}
