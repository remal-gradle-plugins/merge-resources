package name.remal.gradle_plugins.merge_resources.mergers;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static name.remal.gradle_plugins.toolkit.FunctionUtils.toSubstringedBefore;

import com.google.common.annotations.VisibleForTesting;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import org.gradle.api.file.RelativePath;

public abstract class MetaInfServicesMerger extends ResourceMerger {

    private static final Pattern NEW_LINES = Pattern.compile("[\\n\\r]+");


    @Override
    public Collection<String> getIncludes() {
        return List.of("META-INF/services/*");
    }

    @Override
    public Collection<String> getExcludes() {
        return List.of("META-INF/services/org.codehaus.groovy.runtime.ExtensionModule");
    }

    @Override
    public void merge(
        RelativePath relativePath,
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        mergeMetaInfServicesTo(
            paths,
            outputStream
        );
    }

    @VisibleForTesting
    static void mergeMetaInfServicesTo(
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        var services = new LinkedHashSet<String>();
        for (var path : paths) {
            var content = readString(path);

            NEW_LINES.splitAsStream(content)
                .map(toSubstringedBefore("#"))
                .map(String::trim)
                .filter(ObjectUtils::isNotEmpty)
                .forEach(services::add);
        }

        var mergedContent = join("\n", services);
        var bytes = mergedContent.getBytes(UTF_8);
        outputStream.write(bytes);
    }

}
