package name.remal.gradle_plugins.merge_resources;

import static java.util.Collections.unmodifiableCollection;
import static lombok.AccessLevel.PUBLIC;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.gradle.api.file.RelativePath;

@RequiredArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
abstract class CustomResourceMerger extends ResourceMerger {

    private final Collection<String> includes;
    private final CustomResourceMergerFunction merger;

    @Override
    protected Collection<String> getIncludes() {
        return unmodifiableCollection(includes);
    }

    @Override
    protected InputStream merge(RelativePath relativePath, Collection<File> files) throws Throwable {
        return merger.merge(relativePath, files);
    }

}
