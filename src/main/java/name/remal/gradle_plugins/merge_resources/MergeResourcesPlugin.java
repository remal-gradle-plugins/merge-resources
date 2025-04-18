package name.remal.gradle_plugins.merge_resources;

import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getStringProperty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.doNotInline;
import static name.remal.gradle_plugins.toolkit.TaskUtils.doBeforeTaskExecution;
import static name.remal.gradle_plugins.toolkit.TaskUtils.isTaskConfigurable;

import java.io.File;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractCopyTask;

public abstract class MergeResourcesPlugin implements Plugin<Project> {

    public static final String MERGE_RESOURCES_EXTENSION_NAME = doNotInline("mergeResources");

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create(MERGE_RESOURCES_EXTENSION_NAME, MergeResourcesExtension.class);
        project.getTasks().withType(AbstractCopyTask.class).configureEach(task ->
            configureCopyTask(task, extension)
        );
    }

    private static void configureCopyTask(AbstractCopyTask task, MergeResourcesExtension extension) {
        doBeforeTaskExecution(task, it ->
            beforeTaskExecution(it, extension)
        );
    }

    @SuppressWarnings("java:S2629")
    private static void beforeTaskExecution(AbstractCopyTask task, MergeResourcesExtension extension) {
        var mergedFilesDir = new File(task.getTemporaryDir().getAbsolutePath() + ".merged-files");
        task.from(mergedFilesDir);

        if (!isTaskConfigurable(task)) {
            task.getLogger().warn(
                "Resources can't be merged, as task is not configurable."
                    + " If you see this message, please report it to {}.",
                getStringProperty("repository.html-url")
            );
            return;
        }

        var mergers = extension.getAllEnabledResourceMergers();
        mergers.forEach(merger ->
            task.doFirst(
                merger.toString(),
                new CopyTaskActionMerge(mergedFilesDir, merger)
            )
        );

        task.doFirst("Create " + mergedFilesDir, new CopyTaskActionCreateMergedFilesDir(mergedFilesDir));
    }

}
