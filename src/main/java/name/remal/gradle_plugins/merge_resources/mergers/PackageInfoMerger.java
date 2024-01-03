package name.remal.gradle_plugins.merge_resources.mergers;

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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.gradle.api.file.RelativePath;
import org.objectweb.asm.tree.ClassNode;

public abstract class PackageInfoMerger extends ResourceMerger {

    @Override
    protected Collection<String> getIncludes() {
        return singletonList("**/package-info.class");
    }

    @Override
    protected InputStream merge(RelativePath relativePath, Collection<File> files) throws Throwable {
        return mergePackageInfos(files.stream()
            .map(File::toPath)
            .collect(toList())
        );
    }

    @VisibleForTesting
    static InputStream mergePackageInfos(Collection<Path> paths) throws Throwable {
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

        return newInputStream(resultPath);
    }

}
