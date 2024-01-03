package name.remal.gradle_plugins.merge_resources;

import lombok.SneakyThrows;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.AbstractCopyTask;

abstract class AbstractCopyTaskAction implements Action<Task> {

    protected abstract void execute(AbstractCopyTask task) throws Throwable;

    @Override
    @SneakyThrows
    public final void execute(Task task) {
        execute((AbstractCopyTask) task);
    }

}
