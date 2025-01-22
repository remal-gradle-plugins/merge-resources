package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.io.ByteStreams.copy;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.util.Comparator.comparingInt;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.getAnnotationsCount;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.getBytecodeAnnotations;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.readClassNode;

import com.google.common.annotations.VisibleForTesting;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.gradle.api.file.RelativePath;
import org.objectweb.asm.tree.ClassNode;

public abstract class PackageInfoMerger extends ResourceMerger {

    @Override
    public Collection<String> getIncludes() {
        return List.of("**/package-info.class");
    }

    @Override
    public void merge(
        RelativePath relativePath,
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        mergePackageInfosTo(
            paths,
            outputStream
        );
    }

    @VisibleForTesting
    static void mergePackageInfosTo(
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        if (paths.size() <= 1) {
            throw new IllegalArgumentException("paths must have multiple elements");
        }

        var classNodes = new LinkedHashMap<Path, ClassNode>();
        for (var path : paths) {
            classNodes.put(path, readClassNode(path));
        }

        var resultEntry = classNodes.entrySet().stream()
            .max(comparingInt(entry -> getAnnotationsCount(entry.getValue())))
            .orElseThrow();

        var resultPath = resultEntry.getKey();
        classNodes.keySet().removeIf(resultPath::equals);

        var resultClassNode = resultEntry.getValue();
        var resultAnnotations = getBytecodeAnnotations(resultClassNode);
        classNodes.forEach((path, classNode) -> {
            var annotations = getBytecodeAnnotations(classNode);
            for (var annotation : annotations) {
                if (!resultAnnotations.contains(annotation)) {
                    throw new ResourcesToMergeAreInconsistentException(format(
                        "%s contains annotation that does NOT present in %s: %s",
                        path,
                        resultPath,
                        annotation
                    ));
                }
            }
        });

        try (var inputStream = newInputStream(resultPath)) {
            copy(inputStream, outputStream);
        }
    }

}
