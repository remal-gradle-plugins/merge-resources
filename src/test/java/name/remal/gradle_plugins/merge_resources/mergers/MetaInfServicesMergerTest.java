package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.jimfs.Configuration.unix;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static name.remal.gradle_plugins.toolkit.InputOutputStreamUtils.readStringFromStream;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S5778")
class MetaInfServicesMergerTest {

    final FileSystem fs = Jimfs.newFileSystem(unix());

    @Test
    void commentsBlankStringsAndDuplicatesAreRemoved() throws Throwable {
        val path = fs.getPath("/1");
        write(path, join(
            "\n",
            "",
            " # comment",
            "\t",
            "impl # 1",
            "impl # 2",
            "impl # 3"
        ).getBytes(UTF_8));

        try (val inputStream = MetaInfServicesMerger.mergeMetaInfServices(singletonList(path))) {
            val content = readStringFromStream(inputStream, UTF_8);
            assertThat(content).isEqualTo(join(
                "\n",
                "impl"
            ));
        }
    }

    @Test
    void filesAreMerged() throws Throwable {
        val path1 = fs.getPath("/1");
        write(path1, join(
            "\n",
            "impl1"
        ).getBytes(UTF_8));

        val path2 = fs.getPath("/2");
        write(path2, join(
            "\n",
            "impl2"
        ).getBytes(UTF_8));

        try (val inputStream = MetaInfServicesMerger.mergeMetaInfServices(asList(path1, path2))) {
            val content = readStringFromStream(inputStream, UTF_8);
            assertThat(content).isEqualTo(join(
                "\n",
                "impl1",
                "impl2"
            ));
        }
    }

}
