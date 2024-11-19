package name.remal.gradle_plugins.merge_resources.mergers;

import static java.util.Collections.enumeration;
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.gradle.api.file.RelativePath;

public abstract class Log4j2PluginsMerger extends ResourceMerger {

    @Override
    public Collection<String> getIncludes() {
        return ImmutableList.of("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
    }

    @Override
    public void merge(
        RelativePath relativePath,
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        mergeLog4j2PluginsTo(
            paths,
            outputStream
        );
    }

    @VisibleForTesting
    static void mergeLog4j2PluginsTo(
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        val pluginCache = new PluginCache();

        val urls = paths.stream().map(Log4j2PluginsMerger::pathToUrl).collect(toList());
        pluginCache.loadCacheFiles(enumeration(urls));

        pluginCache.writeCache(outputStream);
    }

    @SneakyThrows
    private static URL pathToUrl(Path path) {
        return path.toUri().toURL();
    }

}
