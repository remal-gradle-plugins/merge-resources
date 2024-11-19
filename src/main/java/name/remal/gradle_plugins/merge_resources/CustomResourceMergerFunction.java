package name.remal.gradle_plugins.merge_resources;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import org.gradle.api.file.RelativePath;

@FunctionalInterface
public interface CustomResourceMergerFunction {

    void mergeTo(
        RelativePath relativePath,
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable;

}
