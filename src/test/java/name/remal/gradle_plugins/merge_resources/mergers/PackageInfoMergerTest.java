package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.jimfs.Configuration.unix;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.getBytecode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.getDescriptor;

import com.google.common.jimfs.Jimfs;
import java.io.ByteArrayOutputStream;
import java.nio.file.FileSystem;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

@SuppressWarnings("java:S5778")
class PackageInfoMergerTest {

    final FileSystem fs = Jimfs.newFileSystem(unix());

    @AfterEach
    void afterEach() throws Throwable {
        fs.close();
    }

    @Test
    @SuppressWarnings("VariableDeclarationUsageDistance")
    void packageInfoWithMostNumberOfAnnotationsIsTaken() throws Throwable {
        val path1 = fs.getPath("/1");
        val classNode1 = new ClassNode();
        classNode1.version = V1_8;
        classNode1.access = ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC;
        classNode1.name = "pkg/package-info";
        classNode1.superName = "java/lang/Object";
        classNode1.visibleAnnotations = asList(
            new AnnotationNode(getDescriptor(VisibleForTesting.class)),
            new AnnotationNode(getDescriptor(NotNull.class)),
            new AnnotationNode(getDescriptor(Contract.class))
        );
        val bytes1 = getBytecode(classNode1);
        write(path1, bytes1);

        val path2 = fs.getPath("/2");
        val classNode2 = new ClassNode();
        classNode2.version = V1_8;
        classNode2.access = ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC;
        classNode2.name = "pkg/package-info";
        classNode2.superName = "java/lang/Object";
        classNode2.visibleAnnotations = asList(
            new AnnotationNode(getDescriptor(VisibleForTesting.class)),
            new AnnotationNode(getDescriptor(NotNull.class))
        );
        val bytes2 = getBytecode(classNode2);
        write(path2, bytes2);

        try (val outputStream = new ByteArrayOutputStream()) {
            PackageInfoMerger.mergePackageInfosTo(asList(path1, path2), outputStream);

            val bytes = outputStream.toByteArray();
            assertThat(bytes).isEqualTo(bytes1);
        }
    }

    @Test
    @SuppressWarnings({"try", "VariableDeclarationUsageDistance"})
    void packageInfoWithDifferentAnnotationsAreNotMerged() throws Throwable {
        val path1 = fs.getPath("/1");
        val classNode1 = new ClassNode();
        classNode1.version = V1_8;
        classNode1.access = ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC;
        classNode1.name = "pkg/package-info";
        classNode1.superName = "java/lang/Object";
        classNode1.visibleAnnotations = asList(
            new AnnotationNode(getDescriptor(NotNull.class)),
            new AnnotationNode(getDescriptor(Contract.class))
        );
        val bytes1 = getBytecode(classNode1);
        write(path1, bytes1);

        val path2 = fs.getPath("/2");
        val classNode2 = new ClassNode();
        classNode2.version = V1_8;
        classNode2.access = ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC;
        classNode2.name = "pkg/package-info";
        classNode2.superName = "java/lang/Object";
        classNode2.visibleAnnotations = singletonList(
            new AnnotationNode(getDescriptor(Nullable.class))
        );
        val bytes2 = getBytecode(classNode2);
        write(path2, bytes2);

        try (val outputStream = new ByteArrayOutputStream()) {
            assertThrows(ResourcesToMergeAreInconsistentException.class, () ->
                PackageInfoMerger.mergePackageInfosTo(asList(path1, path2), outputStream)
            );
        }
    }

}
