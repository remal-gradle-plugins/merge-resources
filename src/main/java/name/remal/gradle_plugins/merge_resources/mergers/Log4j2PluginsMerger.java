package name.remal.gradle_plugins.merge_resources.mergers;

import static java.util.Collections.enumeration;
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.gradle.api.file.RelativePath;

/**
 * Inspired by
 * <a href="https://github.com/Corionis/MergeLog4j2Plugins/blob/6464ac1c8a9b079d2c72145f6ed02e01eaf9b0f6/src/com/corionis/mergeLog4j2Plugins/Main.java#L47">github.com/Corionis/MergeLog4j2Plugins</a>.
 */
public abstract class Log4j2PluginsMerger extends ResourceMerger {

    @Override
    protected Collection<String> getIncludes() {
        return ImmutableList.of("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
    }

    @Override
    protected void mergeTo(
            RelativePath relativePath,
            Collection<File> files,
            OutputStream outputStream
    ) throws Throwable {
        mergeLog4j2PluginsTo(
                files.stream()
                        .map(File::toPath)
                        .collect(toList()),
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
