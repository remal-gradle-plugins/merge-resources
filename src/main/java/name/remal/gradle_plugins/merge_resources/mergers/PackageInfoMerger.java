package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.io.ByteStreams.copy;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.getAnnotationsCount;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.getBytecodeAnnotations;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.readClassNode;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.gradle.api.file.RelativePath;
import org.objectweb.asm.tree.ClassNode;

public abstract class PackageInfoMerger extends ResourceMerger {

    @Override
    public Collection<String> getIncludes() {
        return singletonList("**/package-info.class");
    }

    @Override
    public void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        OutputStream outputStream
    ) throws Throwable {
        mergePackageInfosTo(
            files.stream()
                .map(File::toPath)
                .collect(toList()),
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

        val classNodes = new LinkedHashMap<Path, ClassNode>();
        for (val path : paths) {
            classNodes.put(path, readClassNode(path));
        }

        val resultEntry = classNodes.entrySet().stream()
            .max(comparingInt(entry -> getAnnotationsCount(entry.getValue())))
            .get();

        val resultPath = resultEntry.getKey();
        classNodes.keySet().removeIf(resultPath::equals);

        val resultClassNode = resultEntry.getValue();
        val resultAnnotations = getBytecodeAnnotations(resultClassNode);
        classNodes.forEach((path, classNode) -> {
            val annotations = getBytecodeAnnotations(classNode);
            for (val annotation : annotations) {
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

        try (val inputStream = newInputStream(resultPath)) {
            copy(inputStream, outputStream);
        }
    }

}
