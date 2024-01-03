package name.remal.gradle_plugins.merge_resources;

import static java.nio.file.Files.createDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;

import java.io.File;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.gradle.api.tasks.AbstractCopyTask;

@RequiredArgsConstructor
@CustomLog
class CopyTaskActionCreateMergedFilesDir extends AbstractCopyTaskAction {

    private final File mergedFilesDir;

    @Override
    protected void execute(AbstractCopyTask task) throws Throwable {
        logger.debug("Creating a directory for merged files: {}", mergedFilesDir);
        deleteRecursively(mergedFilesDir.toPath());
        createDirectories(mergedFilesDir.toPath());
    }

}
