package name.remal.gradle_plugins.merge_resources;

import static com.google.common.io.ByteStreams.copy;
import static java.nio.file.Files.newOutputStream;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.RelativePath;
import org.gradle.api.tasks.AbstractCopyTask;

@RequiredArgsConstructor
@CustomLog
class CopyTaskActionMerge extends AbstractCopyTaskAction {

    private final File mergedFilesDir;
    private final ResourceMerger merger;

    @Override
    protected void execute(AbstractCopyTask task) {
        val allFilesToMerge = getFilesToMerge(task, merger);
        allFilesToMerge.values().removeIf(files -> files.size() <= 1);
        if (allFilesToMerge.isEmpty()) {
            return;
        }

        allFilesToMerge.forEach((relativePath, files) ->
            mergeRelativePathResources(mergedFilesDir, merger, relativePath, files)
        );

        task.exclude(element -> {
            val filesToMerge = allFilesToMerge.get(element.getRelativePath());
            if (filesToMerge != null) {
                return filesToMerge.contains(element.getFile());
            }
            return false;
        });
    }

    @ReliesOnInternalGradleApi
    private static Map<RelativePath, Collection<File>> getFilesToMerge(AbstractCopyTask task, ResourceMerger merger) {
        val includes = merger.getIncludes();
        if (isEmpty(includes)) {
            throw new IllegalStateException("No includes set for " + merger);
        }

        Map<RelativePath, Collection<File>> allFilesToMerge = new LinkedHashMap<>();
        task.getRootSpec().buildRootResolver().getAllSource()
            .matching(filter -> {
                filter.include(includes);
                filter.exclude(merger.getExcludes());
            })
            .visit(details -> {
                if (!details.isDirectory()) {
                    val filesToMerge = allFilesToMerge.computeIfAbsent(
                        details.getRelativePath(),
                        __ -> new LinkedHashSet<>()
                    );
                    filesToMerge.add(details.getFile());
                }
            });

        return allFilesToMerge;
    }

    @SneakyThrows
    private static void mergeRelativePathResources(
        File mergedFilesDir,
        ResourceMerger merger,
        RelativePath relativePath,
        Collection<File> files
    ) {
        try (val inputStream = merger.merge(relativePath, files)) {
            val targetFilePath = new File(mergedFilesDir, relativePath.toString()).toPath();
            createParentDirectories(targetFilePath);

            try (val outputStream = newOutputStream(targetFilePath)) {
                copy(inputStream, outputStream);
            }
        }
    }

}
