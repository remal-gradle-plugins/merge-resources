package name.remal.gradle_plugins.merge_resources.mergers;

import static java.nio.file.Files.newInputStream;
import static java.util.Locale.ROOT;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Builder;
import lombok.Value;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.gradle.api.file.RelativePath;

/**
 * See
 * <a href="https://github.com/apache/logging-log4j2/blob/caffa21f6409c7e9402d52c98072cf0b68198401/log4j-core/src/main/java/org/apache/logging/log4j/core/config/plugins/processor/PluginCache.java">org.apache.logging.log4j.core.config.plugins.processor.PluginCache</a>.
 */
public abstract class Log4j2PluginsMerger extends ResourceMerger {

    @Override
    public Collection<String> getIncludes() {
        return List.of("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
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
        var categories = new TreeMap<String, Map<String, Log4jPlugin>>();

        for (var path : paths) {
            try (var in = new DataInputStream(newInputStream(path))) {
                var categoriesCount = in.readInt();
                for (int categoryNumber = 1; categoryNumber <= categoriesCount; categoryNumber++) {
                    var category = in.readUTF();
                    var plugins = categories.computeIfAbsent(
                        category.toLowerCase(ROOT),
                        __ -> new TreeMap<>()
                    );

                    var pluginsCount = in.readInt();
                    for (int pluginNumber = 1; pluginNumber <= pluginsCount; pluginNumber++) {
                        var key = in.readUTF();
                        var className = in.readUTF();
                        var name = in.readUTF();
                        var printable = in.readBoolean();
                        var defer = in.readBoolean();
                        plugins.computeIfAbsent(key, currentKey ->
                            Log4jPlugin.builder()
                                .key(key)
                                .className(className)
                                .name(name)
                                .printable(printable)
                                .defer(defer)
                                .build()
                        );
                    }
                }
            }
        }

        try (var out = new DataOutputStream(outputStream)) {
            out.writeInt(categories.size());
            for (var categoryEntry : categories.entrySet()) {
                out.writeUTF(categoryEntry.getKey());

                var plugins = categoryEntry.getValue();
                out.writeInt(plugins.size());
                for (var pluginEntry : plugins.entrySet()) {
                    var plugin = pluginEntry.getValue();
                    out.writeUTF(plugin.getKey());
                    out.writeUTF(plugin.getClassName());
                    out.writeUTF(plugin.getName());
                    out.writeBoolean(plugin.isPrintable());
                    out.writeBoolean(plugin.isDefer());
                }
            }
        }
    }

    @Value
    @Builder
    private static class Log4jPlugin {
        String key;
        String className;
        String name;
        boolean printable;
        boolean defer;
    }

}
