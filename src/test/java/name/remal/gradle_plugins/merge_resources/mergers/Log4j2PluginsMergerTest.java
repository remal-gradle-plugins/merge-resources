package name.remal.gradle_plugins.merge_resources.mergers;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.emptyMap;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrows;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringAfter;
import static name.remal.gradle_plugins.toolkit.StringUtils.substringBefore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;

class Log4j2PluginsMergerTest {

    private static final ClassLoader RESOURCE_LOADER =
        Optional.ofNullable(Log4j2PluginsMergerTest.class.getClassLoader())
            .orElse(getSystemClassLoader());

    @Test
    @SuppressWarnings("resource")
    void autoScenario() throws Throwable {
        val urls = list(RESOURCE_LOADER
            .getResources("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat")
        );
        val paths = urls.stream()
            .map(sneakyThrows(URL::toURI))
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

        try (val outputStream = new ByteArrayOutputStream()) {
            assertDoesNotThrow(() ->
                Log4j2PluginsMerger.mergeLog4j2PluginsTo(paths, outputStream)
            );
        }
    }

}
