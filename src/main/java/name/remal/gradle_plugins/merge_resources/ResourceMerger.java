package name.remal.gradle_plugins.merge_resources;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.InputStream;
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
    protected abstract Collection<String> getIncludes();

    @Internal
    protected Collection<String> getExcludes() {
        return emptyList();
    }

    @MustBeClosed
    protected abstract InputStream merge(RelativePath relativePath, Collection<File> files) throws Throwable;


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
