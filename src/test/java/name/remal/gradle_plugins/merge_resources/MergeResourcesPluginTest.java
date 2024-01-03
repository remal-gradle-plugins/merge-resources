package name.remal.gradle_plugins.merge_resources;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
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
    void test() {
        assertTrue(project.getPlugins().hasPlugin(MergeResourcesPlugin.class));
    }

}
