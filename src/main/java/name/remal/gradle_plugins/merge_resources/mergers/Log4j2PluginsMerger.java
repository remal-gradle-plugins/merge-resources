package name.remal.gradle_plugins.merge_resources.mergers;

import static java.nio.file.Files.newInputStream;
import static java.util.Collections.singletonList;
import static java.util.Locale.ROOT;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import lombok.Builder;
import lombok.Value;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.gradle.api.file.RelativePath;

/**
 * See
 * <a href="https://github.com/apache/logging-log4j2/blob/caffa21f6409c7e9402d52c98072cf0b68198401/log4j-core/src/main/java/org/apache/logging/log4j/core/config/plugins/processor/PluginCache.java">org.apache.logging.log4j.core.config.plugins.processor.PluginCache</a>.
 */
public abstract class Log4j2PluginsMerger extends ResourceMerger {

    @Override
    public Collection<String> getIncludes() {
        return singletonList("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
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
        val categories = new TreeMap<String, Map<String, Log4jPlugin>>();

        for (val path : paths) {
            try (val in = new DataInputStream(newInputStream(path))) {
                val categoriesCount = in.readInt();
                for (int categoryNumber = 1; categoryNumber <= categoriesCount; categoryNumber++) {
                    val category = in.readUTF();
                    val plugins = categories.computeIfAbsent(
                        category.toLowerCase(ROOT),
                        __ -> new TreeMap<>()
                    );

                    val pluginsCount = in.readInt();
                    for (int pluginNumber = 1; pluginNumber <= pluginsCount; pluginNumber++) {
                        val key = in.readUTF();
                        val className = in.readUTF();
                        val name = in.readUTF();
                        val printable = in.readBoolean();
                        val defer = in.readBoolean();
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

        try (val out = new DataOutputStream(outputStream)) {
            out.writeInt(categories.size());
            for (val categoryEntry : categories.entrySet()) {
                out.writeUTF(categoryEntry.getKey());

                val plugins = categoryEntry.getValue();
                out.writeInt(plugins.size());
                for (val pluginEntry : plugins.entrySet()) {
                    val plugin = pluginEntry.getValue();
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
