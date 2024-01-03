package name.remal.gradle_plugins.merge_resources.mergers;

import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.ModuleRequireNode;

@NoArgsConstructor(access = PRIVATE)
abstract class BytecodeUtils {

    public static ClassNode readClassNode(File file) {
        return readClassNode(file.toPath());
    }

    @SneakyThrows
    public static ClassNode readClassNode(Path path) {
        val bytes = readAllBytes(path);

        final ClassReader classReader;
        try {
            classReader = new ClassReader(bytes);
        } catch (IllegalArgumentException exception) {
            throw new TooOldAsmVersionException(
                "Can't parse bytecode file " + path + ". "
                    + "Version of the plugin dependency `org.ow2.asm:asm` is too old. "
                    + "Update `name.remal.merge-resources` plugin to a newer version.",
                exception
            );
        }

        val classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    public static byte[] getBytecode(ClassNode classNode) {
        val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    @Nullable
    @Contract("null -> null; !null -> !null")
    public static String internalClassNameToClassName(@Nullable String internalClassName) {
        if (internalClassName == null) {
            return null;
        }
        return internalClassName.replace('/', '.');
    }

    public static int getAnnotationsCount(ClassNode classNode) {
        int count = 0;
        if (classNode.visibleAnnotations != null) {
            count += classNode.visibleAnnotations.size();
        }
        if (classNode.invisibleAnnotations != null) {
            count += classNode.invisibleAnnotations.size();
        }
        return count;
    }

    public static List<BytecodeAnnotation> getBytecodeAnnotations(ClassNode classNode) {
        return Stream.of(classNode.visibleAnnotations, classNode.invisibleAnnotations)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .map(BytecodeUtils::toBytecodeAnnotation)
            .collect(toList());
    }

    public static BytecodeAnnotation toBytecodeAnnotation(AnnotationNode node) {
        if (isEmpty(node.values)) {
            return new BytecodeAnnotation(node.desc, emptyMap());
        }

        val values = new LinkedHashMap<String, Object>();
        for (int i = 0; i < node.values.size(); i += 2) {
            val name = (String) node.values.get(i);
            val value = toBytecodeAnnotationValue(node.values.get(i + 1));
            values.put(name, value);
        }
        return new BytecodeAnnotation(node.desc, values);
    }

    private static Object toBytecodeAnnotationValue(Object object) {
        if (object instanceof List<?>) {
            return ((List<?>) object).stream()
                .map(BytecodeUtils::toBytecodeAnnotationValue)
                .collect(toList());

        } else if (object instanceof String[]) {
            val array = (String[]) object;
            return new BytecodeAnnotationEnumValue(array[0], array[1]);

        } else if (object instanceof AnnotationNode) {
            return toBytecodeAnnotation((AnnotationNode) object);

        } else {
            return object;
        }
    }

    public static BytecodeModuleRequire toBytecodeModuleRequire(ModuleRequireNode node) {
        return new BytecodeModuleRequire(node.module, new BytecodeModifiers(node.access), node.version);
    }

}
