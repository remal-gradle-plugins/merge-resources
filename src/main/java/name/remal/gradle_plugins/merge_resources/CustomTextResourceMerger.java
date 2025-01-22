package name.remal.gradle_plugins.merge_resources;

import static lombok.AccessLevel.PUBLIC;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.gradle.api.file.RelativePath;

@RequiredArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
abstract class CustomTextResourceMerger extends ResourceMerger {

    private final Collection<String> includes;
    private final Charset charset;
    private final CustomTextResourceMergerFunction merger;

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
        try (var writer = new OutputStreamWriter(outputStream, charset)) {
            try (var printWriter = new PrintWriter(writer)) {
                merger.mergeTo(relativePath, paths, printWriter);
            }
        }
    }

}
