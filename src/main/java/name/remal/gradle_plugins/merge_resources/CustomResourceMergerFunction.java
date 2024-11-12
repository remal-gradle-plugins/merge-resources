package name.remal.gradle_plugins.merge_resources;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import org.gradle.api.file.RelativePath;

@FunctionalInterface
public interface CustomResourceMergerFunction {

    void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        OutputStream outputStream
    ) throws Throwable;

}
