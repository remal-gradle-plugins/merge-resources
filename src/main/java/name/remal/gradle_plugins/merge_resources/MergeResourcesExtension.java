package name.remal.gradle_plugins.merge_resources;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.LinkedHashSet;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import name.remal.gradle_plugins.merge_resources.mergers.MetaInfServicesMerger;
import name.remal.gradle_plugins.merge_resources.mergers.ModuleInfoMerger;
import name.remal.gradle_plugins.merge_resources.mergers.PackageInfoMerger;
import name.remal.gradle_plugins.merge_resources.mergers.SpringFactoriesMerger;
import name.remal.gradle_plugins.merge_resources.mergers.SpringImportsMerger;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

@Getter
@Setter
public abstract class MergeResourcesExtension {

    public abstract ListProperty<ResourceMerger> getResourceMergers();

    public void addResourceMerger(
        Collection<String> includes,
        CustomResourceMergerFunction resourceMerger
    ) {
        if (includes.isEmpty()) {
            throw new IllegalArgumentException("includes must not be empty");
        }

        val merger = getObjects().newInstance(
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


    private final MetaInfServicesMerger metaInfServices = getObjects().newInstance(MetaInfServicesMerger.class);

    public void metaInfServices(Action<MetaInfServicesMerger> action) {
        action.execute(metaInfServices);
    }


    private final PackageInfoMerger packageInfo = getObjects().newInstance(PackageInfoMerger.class);

    public void packageInfo(Action<PackageInfoMerger> action) {
        action.execute(packageInfo);
    }


    private final ModuleInfoMerger moduleInfo = getObjects().newInstance(ModuleInfoMerger.class);

    public void moduleInfo(Action<ModuleInfoMerger> action) {
        action.execute(moduleInfo);
    }


    private final SpringFactoriesMerger springFactories = getObjects().newInstance(SpringFactoriesMerger.class);

    public void springFactories(Action<SpringFactoriesMerger> action) {
        action.execute(springFactories);
    }


    private final SpringImportsMerger springImports = getObjects().newInstance(SpringImportsMerger.class);

    public void springImports(Action<SpringImportsMerger> action) {
        action.execute(springImports);
    }


    @Inject
    protected abstract ObjectFactory getObjects();

}
