package name.remal.gradle_plugins.merge_resources;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import org.gradle.api.file.RelativePath;

@FunctionalInterface
public interface CustomTextResourceMergerFunction {

    void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        PrintWriter writer
    ) throws Throwable;

}
