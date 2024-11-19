package name.remal.gradle_plugins.merge_resources;

import static java.nio.file.Files.newOutputStream;
import static java.util.stream.Collectors.toList;
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
        allFilesToMerge.entrySet().removeIf(entry -> {
            val files = entry.getValue();
            if (files.isEmpty()) {
                return true;
            } else if (files.size() == 1) {
                val relativePath = entry.getKey();
                logger.debug(
                    "{}: only one file for relative path `{}`, skip merging: {}",
                    this,
                    relativePath,
                    files.iterator().next()
                );
                return true;
            }
            return false;
        });
        if (allFilesToMerge.isEmpty()) {
            return;
        }

        allFilesToMerge.forEach((relativePath, files) ->
            mergeRelativePathResources(mergedFilesDir, merger, relativePath, files)
        );

        task.exclude(element -> {
            val filesToMerge = allFilesToMerge.get(element.getRelativePath());
            if (filesToMerge != null) {
                val file = element.getFile();
                if (filesToMerge.contains(file)) {
                    logger.debug("{}: excluding file, as it was merged: {}", this, file);
                    return true;
                }
            }
            return false;
        });
    }

    @ReliesOnInternalGradleApi
    private Map<RelativePath, Collection<File>> getFilesToMerge(AbstractCopyTask task, ResourceMerger merger) {
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
                if (details.isDirectory()) {
                    return;
                }

                val relativePath = details.getRelativePath();
                val filesToMerge = allFilesToMerge.computeIfAbsent(relativePath, __ -> new LinkedHashSet<>());
                val file = details.getFile();
                filesToMerge.add(file);
                logger.debug(
                    "{}: found merging candidate for for relative path `{}`: {}",
                    this,
                    relativePath,
                    file
                );
            });

        return allFilesToMerge;
    }

    @SneakyThrows
    private void mergeRelativePathResources(
        File mergedFilesDir,
        ResourceMerger merger,
        RelativePath relativePath,
        Collection<File> files
    ) {
        logger.debug("{}: merging files for relative path `{}`: {}", this, relativePath, files);

        val targetFilePath = new File(mergedFilesDir, relativePath.toString()).toPath();
        createParentDirectories(targetFilePath);

        try (val outputStream = newOutputStream(targetFilePath)) {
            merger.merge(
                relativePath,
                files.stream()
                    .map(File::toPath)
                    .collect(toList()),
                outputStream
            );
        }
    }

}
