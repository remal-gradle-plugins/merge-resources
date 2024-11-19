package name.remal.gradle_plugins.merge_resources.mergers;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.emptyMap;
import static java.util.Collections.enumeration;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrows;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringAfter;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringBefore;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.val;
import name.remal.gradle_plugins.toolkit.UriUtils;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.junit.jupiter.api.Test;

class Log4j2PluginsMergerTest {

    private static final ClassLoader RESOURCE_LOADER =
        Optional.ofNullable(Log4j2PluginsMergerTest.class.getClassLoader())
            .orElse(getSystemClassLoader());

    @Test
    @SuppressWarnings("resource")
    void equalToPluginCache() throws Throwable {
        val urls = list(RESOURCE_LOADER
            .getResources("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat")
        );
        val paths = urls.stream()
            .map(UriUtils::toUri)
            .map(sneakyThrows((URI uri) -> {
                val file = substringBefore(uri.toString(), "!", "");
                if (file.isEmpty()) {
                    return Paths.get(uri);
                }

                val fs = newFileSystem(URI.create(file), emptyMap());
                return fs.getPath(substringAfter(uri.toString(), "!", ""));
            }))
            .distinct()
            .collect(toList());

        assertThat(paths).as("paths")
            .hasSizeGreaterThanOrEqualTo(3);

        final byte[] expectedBytes;
        try (val outputStream = new ByteArrayOutputStream()) {
            val pluginCache = new PluginCache();
            pluginCache.loadCacheFiles(enumeration(urls));
            pluginCache.writeCache(outputStream);
            expectedBytes = outputStream.toByteArray();
        }

        final byte[] actualBytes;
        try (val outputStream = new ByteArrayOutputStream()) {
            Log4j2PluginsMerger.mergeLog4j2PluginsTo(paths, outputStream);
            actualBytes = outputStream.toByteArray();
        }

        assertThat(actualBytes)
            .isNotEmpty()
            .isEqualTo(expectedBytes);
    }

}
