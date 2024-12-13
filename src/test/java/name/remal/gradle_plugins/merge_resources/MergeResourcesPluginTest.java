package name.remal.gradle_plugins.merge_resources;

import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.packageNameOf;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectValidations.executeAfterEvaluateActions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.TaskValidations;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class MergeResourcesPluginTest {

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply(MergeResourcesPlugin.class);
    }

    @Test
    void extensionAdded() {
        assertDoesNotThrow(() -> project.getExtensions().getByType(MergeResourcesExtension.class));
        assertDoesNotThrow(() -> project.getExtensions().getByName("mergeResources"));
    }

    @Test
    void addResourceMergerDoesNotThrowException() {
        val mergeResources = project.getExtensions().getByType(MergeResourcesExtension.class);
        assertDoesNotThrow(() ->
            mergeResources.addResourceMerger("*.jar", (relativePath, paths, outputStream) -> {
                throw new UnsupportedOperationException();
            })
        );
    }

    @Test
    void addTextResourceMergerDoesNotThrowException() {
        val mergeResources = project.getExtensions().getByType(MergeResourcesExtension.class);
        assertDoesNotThrow(() ->
            mergeResources.addTextResourceMerger("*.txt", "UTF-8", (relativePath, paths, writer) -> {
                throw new UnsupportedOperationException();
            })
        );
    }

    @Test
    void pluginTasksDoNotHavePropertyProblems() {
        project.getPluginManager().apply("java");

        executeAfterEvaluateActions(project);

        val taskClassNamePrefix = packageNameOf(MergeResourcesPlugin.class) + '.';
        project.getTasks().stream()
            .filter(task -> {
                val taskClass = unwrapGeneratedSubclass(task.getClass());
                return taskClass.getName().startsWith(taskClassNamePrefix);
            })
            .forEach(TaskValidations::assertNoTaskPropertiesProblems);
    }

}
