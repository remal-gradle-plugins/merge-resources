package name.remal.gradle_plugins.merge_resources.mergers;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
class BytecodeModuleRequire {

    @NonNull
    String module;

    @NonNull
    BytecodeModifiers modifiers;

    @Nullable
    String version;

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("requires ");

        var modifiersString = modifiers.toString();
        if (!modifiersString.isEmpty()) {
            sb.append(modifiersString).append(' ');
        }

        sb.append(module);

        if (isNotEmpty(version)) {
            sb.append('@').append(version);
        }

        return sb.toString();
    }

}
