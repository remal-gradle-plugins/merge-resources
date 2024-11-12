package name.remal.gradle_plugins.merge_resources.mergers;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.getBytecode;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.internalClassNameToClassName;
import static name.remal.gradle_plugins.merge_resources.mergers.BytecodeUtils.readClassNode;
import static name.remal.gradle_plugins.merge_resources.mergers.Utils.distinctBy;
import static name.remal.gradle_plugins.merge_resources.mergers.Utils.removeFirst;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.ResourceMerger;
import org.gradle.api.file.RelativePath;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;

public abstract class ModuleInfoMerger extends ResourceMerger {

    @Override
    protected Collection<String> getIncludes() {
        return singletonList("**/module-info.class");
    }

    @Override
    protected void mergeTo(
        RelativePath relativePath,
        Collection<File> files,
        OutputStream outputStream
    ) throws Throwable {
        mergeModuleInfosTo(
            files.stream()
                .map(File::toPath)
                .collect(toList()),
            outputStream
        );
    }

    @VisibleForTesting
    static void mergeModuleInfosTo(
        Collection<Path> paths,
        OutputStream outputStream
    ) throws Throwable {
        if (paths.size() <= 1) {
            throw new IllegalArgumentException("paths must have multiple elements");
        }

        Map<Path, ClassNode> classNodes = new LinkedHashMap<>();
        for (val path : paths) {
            val classNode = readClassNode(path);
            if (classNode.module == null) {
                throw new IllegalStateException("Not a module-info class: " + path);
            }
            classNodes.put(path, classNode);
        }

        checkModuleNodesConsistency(classNodes, "names", it -> it.name);
        checkModuleNodesConsistency(classNodes, "versions", it -> it.version);
        checkModuleNodesConsistency(classNodes, "main class names", it -> internalClassNameToClassName(it.mainClass));
        checkModuleRequireNodesConsistency(classNodes);

        val targetClassNode = removeFirst(classNodes.values());
        mergeInto(classNodes.values(), targetClassNode);

        val bytes = getBytecode(targetClassNode);
        outputStream.write(bytes);
    }

    @SuppressWarnings("java:S3776")
    private static void mergeInto(Collection<ClassNode> classNodes, ClassNode targetClassNode) {
        val targetModuleNode = targetClassNode.module;
        classNodes.forEach(classNode -> {
            val moduleNode = classNode.module;
            targetModuleNode.access |= moduleNode.access;
            if (targetModuleNode.mainClass == null && moduleNode.mainClass != null) {
                targetModuleNode.mainClass = moduleNode.mainClass;
            }
            targetModuleNode.packages = Stream.of(targetModuleNode.packages, moduleNode.packages)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList());
            targetModuleNode.requires = Stream.of(targetModuleNode.requires, moduleNode.requires)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(distinctBy(it -> it.module))
                .collect(toList());
            val exportNodes = Stream.of(targetModuleNode.exports, moduleNode.exports)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
            targetModuleNode.exports = exportNodes.stream()
                .map(it -> it.packaze)
                .distinct()
                .map(pkg -> {
                    val packageExportNodes = exportNodes.stream()
                        .filter(openNode -> pkg.equals(openNode.packaze))
                        .collect(toList());
                    int access = 0;
                    for (val exportNode : packageExportNodes) {
                        access |= exportNode.access;
                    }
                    List<String> modules = new ArrayList<>();
                    for (val exportNode : packageExportNodes) {
                        if (isEmpty(exportNode.modules)) {
                            modules = null;
                            break;
                        }
                        exportNode.modules.stream()
                            .filter(not(modules::contains))
                            .forEach(modules::add);
                    }
                    return new ModuleExportNode(pkg, access, modules);
                })
                .collect(toList());
            val openNodes = Stream.of(targetModuleNode.opens, moduleNode.opens)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
            targetModuleNode.opens = openNodes.stream()
                .map(it -> it.packaze)
                .distinct()
                .map(pkg -> {
                    val packageOpenNodes = openNodes.stream()
                        .filter(openNode -> pkg.equals(openNode.packaze))
                        .collect(toList());
                    int access = 0;
                    for (val openNode : packageOpenNodes) {
                        access |= openNode.access;
                    }
                    List<String> modules = new ArrayList<>();
                    for (val openNode : packageOpenNodes) {
                        if (isEmpty(openNode.modules)) {
                            modules = null;
                            break;
                        }
                        openNode.modules.stream()
                            .filter(not(modules::contains))
                            .forEach(modules::add);
                    }
                    return new ModuleOpenNode(pkg, access, modules);
                })
                .collect(toList());
            targetModuleNode.uses = Stream.of(targetModuleNode.uses, moduleNode.uses)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList());
            val provideNodes = Stream.of(targetModuleNode.provides, moduleNode.provides)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
            targetModuleNode.provides = provideNodes.stream()
                .map(it -> it.service)
                .distinct()
                .map(service -> new ModuleProvideNode(
                    service,
                    provideNodes.stream()
                        .filter(provideNode -> service.equals(provideNode.service))
                        .map(provideNode -> provideNode.providers)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(toList())
                ))
                .collect(toList());
        });
    }

    private static void checkModuleNodesConsistency(
        Map<Path, ClassNode> classNodes,
        String scope,
        Function<ModuleNode, String> getter
    ) {
        Map<String, Path> values = new LinkedHashMap<>();
        classNodes.forEach((path, classNode) -> {
            val value = getter.apply(classNode.module);
            if (value != null) {
                values.putIfAbsent(value, path);
            }
        });
        if (values.size() > 1) {
            throw new ResourcesToMergeAreInconsistentException(format(
                "Module info files can't be merged, as they have different module %s:%n%s",
                scope,
                values.entrySet().stream()
                    .map(entry -> entry.getValue() + ": " + entry.getKey())
                    .collect(joining("\n"))
            ));
        }
    }

    private static void checkModuleRequireNodesConsistency(Map<Path, ClassNode> classNodes) {
        val allRequireNodes = new LinkedHashMap<String, Map<BytecodeModuleRequire, Path>>();
        classNodes.forEach((path, classNode) -> {
            if (isEmpty(classNode.module.requires)) {
                return;
            }

            classNode.module.requires.stream()
                .map(BytecodeUtils::toBytecodeModuleRequire)
                .forEach(requireObject -> {
                    val requireNodes = allRequireNodes.computeIfAbsent(
                        requireObject.getModule(),
                        __ -> new LinkedHashMap<>()
                    );
                    requireNodes.put(requireObject, path);
                });
        });

        allRequireNodes.values().removeIf(it -> it.size() <= 1);
        if (allRequireNodes.isEmpty()) {
            return;
        }

        val requireNodes = allRequireNodes.values().iterator().next();
        throw new ResourcesToMergeAreInconsistentException(format(
            "Module info files can't be merged, as they have inconsistent requires:%n%s",
            requireNodes.entrySet().stream()
                .map(entry -> entry.getValue() + ": " + entry.getKey())
                .collect(joining("\n"))
        ));
    }

}
