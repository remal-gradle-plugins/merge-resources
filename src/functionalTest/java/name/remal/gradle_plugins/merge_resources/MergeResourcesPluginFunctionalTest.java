package name.remal.gradle_plugins.merge_resources;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class MergeResourcesPluginFunctionalTest {

    final GradleProject project;

    @Test
    void test() throws Throwable {
        project.forBuildFile(build -> {
            build.applyPlugin("name.remal.merge-resources");
            project.writeTextFile("dir1/META-INF/services/service", "impl1");
            project.writeTextFile("dir2/META-INF/services/service", "impl2");
            build.block("tasks.create('copyTask', Copy)", copyTask ->
                copyTask.line(join("\n", new String[]{
                    "duplicatesStrategy = 'FAIL'",
                    "from('dir1')",
                    "from('dir2')",
                    "into('target')"
                }))
            );
        });

        project.assertBuildSuccessfully("copyTask");

        var targetPath = project.getProjectDir().toPath().resolve("target/META-INF/services/service");
        var content = new String(readAllBytes(targetPath), UTF_8);
        var implementations = Splitter.on('\n').splitToStream(content)
            .map(String::trim)
            .filter(ObjectUtils::isNotEmpty)
            .collect(toList());
        assertThat(implementations)
            .containsExactlyInAnyOrder(
                "impl1",
                "impl2"
            );
    }

}
