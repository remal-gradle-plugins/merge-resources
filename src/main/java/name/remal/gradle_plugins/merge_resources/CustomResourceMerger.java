package name.remal.gradle_plugins.merge_resources;

import static java.util.Collections.unmodifiableCollection;
import static lombok.AccessLevel.PUBLIC;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.gradle.api.file.RelativePath;

@RequiredArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
abstract class CustomResourceMerger extends ResourceMerger {

    private final Collection<String> includes;
    private final CustomResourceMergerFunction merger;

    @Override
    public Collection<String> getIncludes() {
        return unmodifiableCollection(includes);
    }

    @Override
    public void merge(
        RelativePath relativePath,
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        merger.mergeTo(relativePath, paths, outputStream);
    }

}
