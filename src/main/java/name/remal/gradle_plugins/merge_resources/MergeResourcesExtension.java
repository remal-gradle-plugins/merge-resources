package name.remal.gradle_plugins.merge_resources;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import name.remal.gradle_plugins.merge_resources.mergers.Log4j2PluginsMerger;
import name.remal.gradle_plugins.merge_resources.mergers.MetaInfServicesMerger;
import name.remal.gradle_plugins.merge_resources.mergers.ModuleInfoMerger;
import name.remal.gradle_plugins.merge_resources.mergers.PackageInfoMerger;
import name.remal.gradle_plugins.merge_resources.mergers.SpringFactoriesMerger;
import name.remal.gradle_plugins.merge_resources.mergers.SpringImportsMerger;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.Unmodifiable;

@Getter
@Setter
public abstract class MergeResourcesExtension {

    @Internal
    public abstract ListProperty<ResourceMerger> getResourceMergers();


    public void addResourceMerger(
        Collection<String> includes,
        CustomResourceMergerFunction resourceMerger
    ) {
        if (includes.isEmpty()) {
            throw new IllegalArgumentException("includes must not be empty");
        }

        var merger = getObjects().newInstance(
            CustomResourceMerger.class,
            new LinkedHashSet<>(includes),
            resourceMerger
        );
        getResourceMergers().add(merger);
    }

    public void addResourceMerger(
        String include,
        CustomResourceMergerFunction resourceMerger
    ) {
        addResourceMerger(singletonList(include), resourceMerger);
    }

    public void addTextResourceMerger(
        Collection<String> includes,
        Charset charset,
        CustomTextResourceMergerFunction resourceMerger
    ) {
        if (includes.isEmpty()) {
            throw new IllegalArgumentException("includes must not be empty");
        }

        var merger = getObjects().newInstance(
            CustomTextResourceMerger.class,
            new LinkedHashSet<>(includes),
            charset,
            resourceMerger
        );
        getResourceMergers().add(merger);
    }

    public void addTextResourceMerger(
        Collection<String> includes,
        String charset,
        CustomTextResourceMergerFunction resourceMerger
    ) {
        addTextResourceMerger(includes, Charset.forName(charset), resourceMerger);
    }

    public void addTextResourceMerger(
        Collection<String> includes,
        CustomTextResourceMergerFunction resourceMerger
    ) {
        addTextResourceMerger(includes, UTF_8, resourceMerger);
    }

    public void addTextResourceMerger(
        String include,
        Charset charset,
        CustomTextResourceMergerFunction resourceMerger
    ) {
        addTextResourceMerger(singletonList(include), charset, resourceMerger);
    }

    public void addTextResourceMerger(
        String include,
        String charset,
        CustomTextResourceMergerFunction resourceMerger
    ) {
        addTextResourceMerger(singletonList(include), charset, resourceMerger);
    }

    public void addTextResourceMerger(
        String include,
        CustomTextResourceMergerFunction resourceMerger
    ) {
        addTextResourceMerger(singletonList(include), resourceMerger);
    }


    @Internal
    private final MetaInfServicesMerger metaInfServices = getObjects().newInstance(MetaInfServicesMerger.class);

    public void metaInfServices(Action<MetaInfServicesMerger> action) {
        action.execute(metaInfServices);
    }


    @Internal
    private final PackageInfoMerger packageInfo = getObjects().newInstance(PackageInfoMerger.class);

    public void packageInfo(Action<PackageInfoMerger> action) {
        action.execute(packageInfo);
    }


    @Internal
    private final ModuleInfoMerger moduleInfo = getObjects().newInstance(ModuleInfoMerger.class);

    public void moduleInfo(Action<ModuleInfoMerger> action) {
        action.execute(moduleInfo);
    }


    @Internal
    private final SpringFactoriesMerger springFactories = getObjects().newInstance(SpringFactoriesMerger.class);

    public void springFactories(Action<SpringFactoriesMerger> action) {
        action.execute(springFactories);
    }


    @Internal
    private final SpringImportsMerger springImports = getObjects().newInstance(SpringImportsMerger.class);

    public void springImports(Action<SpringImportsMerger> action) {
        action.execute(springImports);
    }


    @Internal
    private final Log4j2PluginsMerger log4j2PluginsMerger = getObjects().newInstance(Log4j2PluginsMerger.class);

    public void log4j2PluginsMerger(Action<Log4j2PluginsMerger> action) {
        action.execute(log4j2PluginsMerger);
    }


    @Internal
    @Unmodifiable
    public final Collection<ResourceMerger> getAllResourceMergers() {
        return unmodifiableCollection(
            Stream.concat(
                    getResourceMergers().get().stream(),
                    Stream.of(
                        metaInfServices,
                        packageInfo,
                        moduleInfo,
                        springFactories,
                        springImports,
                        log4j2PluginsMerger
                    )
                )
                .collect(toList())
        );
    }

    @Internal
    @Unmodifiable
    public final Collection<ResourceMerger> getAllEnabledResourceMergers() {
        return unmodifiableCollection(
            getAllResourceMergers().stream()
                .filter(merger -> merger.getEnabled().getOrElse(true))
                .collect(toList())
        );
    }


    @Inject
    protected abstract ObjectFactory getObjects();

}
