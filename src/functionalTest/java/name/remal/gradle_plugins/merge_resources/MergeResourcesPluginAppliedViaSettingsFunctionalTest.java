package name.remal.gradle_plugins.merge_resources;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class MergeResourcesPluginAppliedViaSettingsFunctionalTest {

    final GradleProject project;

    @Test
    void appliedViaSettingsIsAppliedToProject() {
        project.forSettingsFile(settings -> settings.applyPlugin("name.remal.merge-resources"));

        // The plugin must NOT be applied via the project's build file: it should reach the project
        // solely through the Settings-level application propagating via GradleLifecycle.beforeProject.
        // `pluginManager.hasPlugin(...)` is captured at configuration time (not inside `doLast`):
        // reading `project` at task execution time is unsupported with the configuration cache.
        project.getBuildFile().line(
            "def isPluginApplied = pluginManager.hasPlugin('name.remal.merge-resources')"
        );
        project.getBuildFile().line(
            "tasks.register('assertPluginApplied') { doLast { assert isPluginApplied } }"
        );

        project.assertBuildSuccessfully("assertPluginApplied");
    }

}
