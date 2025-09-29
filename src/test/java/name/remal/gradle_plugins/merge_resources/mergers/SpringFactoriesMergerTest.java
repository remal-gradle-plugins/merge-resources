package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.jimfs.Configuration.unix;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.jimfs.Jimfs;
import java.io.ByteArrayOutputStream;
import java.nio.file.FileSystem;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"java:S5778", "StringJoin"})
class SpringFactoriesMergerTest {

    final FileSystem fs = Jimfs.newFileSystem(unix());

    @AfterEach
    void afterEach() throws Throwable {
        fs.close();
    }

    @Test
    void commentsBlankStringsAndDuplicatesAreRemoved() throws Throwable {
        var path = fs.getPath("/1");
        write(path, join(
            "\n",
            "",
            " # comment",
            "\t",
            "service =  impl1 , impl1\t, impl2,impl1",
            ""
        ).getBytes(UTF_8));

        try (var outputStream = new ByteArrayOutputStream()) {
            SpringFactoriesMerger.mergeSpringFactoriesTo(singletonList(path), outputStream);

            var content = outputStream.toString(UTF_8);
            assertThat(content).isEqualTo(join(
                "\n",
                "service = \\",
                "impl1,\\",
                "impl2"
            ));
        }
    }

    @Test
    void codeFormatting() throws Throwable {
        var path = fs.getPath("/1");
        write(path, join(
            "\n",
            "service2 = impl21, impl22",
            "service1 = impl11, impl12",
            ""
        ).getBytes(UTF_8));

        try (var outputStream = new ByteArrayOutputStream()) {
            SpringFactoriesMerger.mergeSpringFactoriesTo(singletonList(path), outputStream);

            var content = outputStream.toString(UTF_8);
            assertThat(content).isEqualTo(join(
                "\n",
                "service1 = \\",
                "impl11,\\",
                "impl12",
                "",
                "service2 = \\",
                "impl21,\\",
                "impl22"
            ));
        }
    }

    @Test
    void filesAreMerged() throws Throwable {
        var path1 = fs.getPath("/1");
        write(path1, join(
            "\n",
            "service = impl1"
        ).getBytes(UTF_8));

        var path2 = fs.getPath("/2");
        write(path2, join(
            "\n",
            "service = impl2"
        ).getBytes(UTF_8));

        try (var outputStream = new ByteArrayOutputStream()) {
            SpringFactoriesMerger.mergeSpringFactoriesTo(List.of(path1, path2), outputStream);

            var content = outputStream.toString(UTF_8);
            assertThat(content).isEqualTo(join(
                "\n",
                "service = \\",
                "impl1,\\",
                "impl2"
            ));
        }
    }

}
