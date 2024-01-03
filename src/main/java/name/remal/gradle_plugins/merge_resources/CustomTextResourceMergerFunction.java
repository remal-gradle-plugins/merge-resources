package name.remal.gradle_plugins.merge_resources;

import java.io.File;
import java.util.Collection;
import org.gradle.api.file.RelativePath;

@FunctionalInterface
public interface CustomTextResourceMergerFunction {

    String merge(RelativePath relativePath, Collection<File> files) throws Throwable;

}
