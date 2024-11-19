package name.remal.gradle_plugins.merge_resources;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collection;
import org.gradle.api.file.RelativePath;

@FunctionalInterface
public interface CustomTextResourceMergerFunction {

    void mergeTo(
        RelativePath relativePath,
        Collection<Path> paths,
        PrintWriter writer
    ) throws Throwable;

}
