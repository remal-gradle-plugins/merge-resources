package name.remal.gradle_plugins.merge_resources.mergers;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.FunctionUtils.toSubstringedBefore;
import static name.remal.gradle_plugins.toolkit.InputOutputStreamUtils.readStringFromStream;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import org.gradle.api.file.RelativePath;

public abstract class MetaInfServicesMerger extends ResourceMerger {

    private static final Pattern NEW_LINES = Pattern.compile("[\\n\\r]+");


    @Override
    public Collection<String> getIncludes() {
        return singletonList("META-INF/services/*");
    }

    @Override
    public Collection<String> getExcludes() {
        return singletonList("META-INF/services/org.codehaus.groovy.runtime.ExtensionModule");
    }

    @Override
    public void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        OutputStream outputStream
    ) throws Throwable {
        mergeMetaInfServicesTo(
            files.stream()
                .map(File::toPath)
                .collect(toList()),
            outputStream
        );
    }

    @VisibleForTesting
    static void mergeMetaInfServicesTo(
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        val services = new LinkedHashSet<String>();
        for (val path : paths) {
            final String content;
            try (val inputStream = newInputStream(path)) {
                content = readStringFromStream(inputStream, UTF_8);
            }

            NEW_LINES.splitAsStream(content)
                .map(toSubstringedBefore("#"))
                .map(String::trim)
                .filter(ObjectUtils::isNotEmpty)
                .forEach(services::add);
        }

        val mergedContent = join("\n", services);
        val bytes = mergedContent.getBytes(UTF_8);
        outputStream.write(bytes);
    }

}
