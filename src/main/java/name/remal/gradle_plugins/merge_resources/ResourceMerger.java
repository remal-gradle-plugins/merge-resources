package name.remal.gradle_plugins.merge_resources;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import org.gradle.api.file.RelativePath;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

public abstract class ResourceMerger {

    public abstract Property<Boolean> getEnabled();

    {
        getEnabled().convention(true);
    }


    @Internal
    public abstract Collection<String> getIncludes();

    @Internal
    public Collection<String> getExcludes() {
        return emptyList();
    }

    public abstract void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        OutputStream outputStream
    ) throws Throwable;


    @Override
    public String toString() {
        return format(
            "%s[includes = %s; excludes = %s]",
            unwrapGeneratedSubclass(this.getClass()).getSimpleName(),
            getIncludes(),
            getExcludes()
        );
    }

}
