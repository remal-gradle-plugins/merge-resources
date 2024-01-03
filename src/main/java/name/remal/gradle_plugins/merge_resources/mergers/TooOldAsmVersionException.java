package name.remal.gradle_plugins.merge_resources.mergers;

import javax.annotation.Nullable;
import org.gradle.api.GradleException;

public class TooOldAsmVersionException extends GradleException {

    TooOldAsmVersionException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
