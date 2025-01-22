package name.remal.gradle_plugins.merge_resources;

import static lombok.AccessLevel.PUBLIC;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.gradle.api.file.RelativePath;

@RequiredArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
abstract class CustomResourceMerger extends ResourceMerger {

    private final Collection<String> includes;
    private final CustomResourceMergerFunction merger;

    @Override
    public Collection<String> getIncludes() {
        return List.copyOf(includes);
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
