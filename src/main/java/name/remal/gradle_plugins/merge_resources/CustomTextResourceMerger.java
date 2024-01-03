package name.remal.gradle_plugins.merge_resources;

import static java.util.Collections.unmodifiableCollection;
import static lombok.AccessLevel.PUBLIC;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
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
    protected InputStream merge(RelativePath relativePath, Collection<File> files) throws Throwable {
        val content = merger.merge(relativePath, files);
        val bytes = content.getBytes(charset);
        return new ByteArrayInputStream(bytes);
    }

}
