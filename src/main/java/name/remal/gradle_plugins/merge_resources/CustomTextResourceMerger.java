package name.remal.gradle_plugins.merge_resources;

import static java.util.Collections.unmodifiableCollection;
import static lombok.AccessLevel.PUBLIC;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.file.RelativePath;

@RequiredArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
abstract class CustomTextResourceMerger extends ResourceMerger {

    private final Collection<String> includes;
    private final Charset charset;
    private final CustomTextResourceMergerFunction merger;

    @Override
    protected Collection<String> getIncludes() {
        return unmodifiableCollection(includes);
    }

    @Override
    protected void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        OutputStream outputStream
    ) throws Throwable {
        try (val writer = new OutputStreamWriter(outputStream, charset)) {
            try (val printWriter = new PrintWriter(writer)) {
                merger.mergeTo(relativePath, files, printWriter);
            }
        }
    }

}
