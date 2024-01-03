package name.remal.gradle_plugins.merge_resources.mergers;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

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
        val sb = new StringBuilder();
        sb.append("requires ");

        val modifiersString = modifiers.toString();
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
