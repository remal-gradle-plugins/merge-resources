package name.remal.gradle_plugins.merge_resources.mergers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.defaultValue;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.loadProperties;
import static org.apache.commons.text.translate.EntityArrays.JAVA_CTRL_CHARS_ESCAPE;

import com.google.common.annotations.VisibleForTesting;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.commons.text.translate.UnicodeEscaper;
import org.gradle.api.file.RelativePath;

public abstract class SpringFactoriesMerger extends ResourceMerger {

    private static final Pattern IMPL_DELIMITER = Pattern.compile(",");

    private static final CharSequenceTranslator ESCAPER = new AggregateTranslator(
        new LookupTranslator(Stream.of("\\", " ", "=", ":", "#", "!")
            .collect(toMap(identity(), it -> "\\" + it))
        ),
        new LookupTranslator(JAVA_CTRL_CHARS_ESCAPE),
        UnicodeEscaper.outsideOf(0x0020, 0x007e)
    );


    @Override
    public Collection<String> getIncludes() {
        return List.of("META-INF/spring.factories");
    }

    @Override
    public void merge(
        RelativePath relativePath,
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        mergeSpringFactoriesTo(
            paths,
            outputStream
        );
    }

    @VisibleForTesting
    static void mergeSpringFactoriesTo(
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        var allServices = new TreeMap<String, Set<String>>();
        for (var path : paths) {
            var properties = loadProperties(path);
            for (var service : properties.stringPropertyNames()) {
                var impls = allServices.computeIfAbsent(service, __ -> new LinkedHashSet<>());
                IMPL_DELIMITER.splitAsStream(defaultValue(properties.getProperty(service)))
                    .map(String::trim)
                    .filter(ObjectUtils::isNotEmpty)
                    .forEach(impls::add);
            }
        }

        allServices.values().removeIf(ObjectUtils::isEmpty);

        var content = new StringBuilder();
        allServices.forEach((service, impls) -> {
            if (content.length() > 0) {
                content.append("\n\n");
            }

            content.append(ESCAPER.translate(service)).append(" = \\\n");
            boolean isFirstImpl = true;
            for (var impl : impls) {
                if (isFirstImpl) {
                    isFirstImpl = false;
                } else {
                    content.append(",\\\n");
                }

                content.append(ESCAPER.translate(impl));
            }
        });
        var bytes = content.toString().getBytes(UTF_8);
        outputStream.write(bytes);
    }

}
