package name.remal.gradle_plugins.merge_resources;

import static java.nio.file.Files.createDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.gradle.api.tasks.AbstractCopyTask;

@RequiredArgsConstructor
class CopyTaskActionCreateDir extends AbstractCopyTaskAction {

    private final File dir;

    @Override
    protected void execute(AbstractCopyTask task) throws Throwable {
        deleteRecursively(dir.toPath());
        createDirectories(dir.toPath());
    }

}
