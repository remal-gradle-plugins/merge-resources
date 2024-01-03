package name.remal.gradle_plugins.merge_resources.mergers;

import org.gradle.api.GradleException;

public class ResourcesToMergeAreInconsistentException extends GradleException {

    ResourcesToMergeAreInconsistentException(String message) {
        super(message);
    }

}
