package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.Collections.unmodifiableMap;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotStatic;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.makeAccessible;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.SneakyThrows;
import lombok.Value;
import org.objectweb.asm.Opcodes;

@Value
public class BytecodeModifiers {

    int access;

    @Override
    public String toString() {
        if (access == 0) {
            return "";
        }

        var sb = new StringBuilder();
        MODIFIER_DESCRIPTIONS.forEach((value, description) -> {
            if ((access & value) != 0) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(description);
            }
        });
        return sb.toString();
    }


    private static final Map<Integer, String> MODIFIER_DESCRIPTIONS = calculateModifierDescriptions();

    @SneakyThrows
    private static Map<Integer, String> calculateModifierDescriptions() {
        Map<Integer, String> result = new TreeMap<>();
        for (var field : Opcodes.class.getDeclaredFields()) {
            if (isNotStatic(field)) {
                continue;
            }
            var prefix = "ACC_";
            if (field.getType() == int.class && field.getName().startsWith(prefix)) {
                var value = (Integer) makeAccessible(field).get(null);
                var description = UPPER_UNDERSCORE.to(LOWER_HYPHEN, field.getName().substring(prefix.length()));
                result.put(value, description);
            }
        }
        return unmodifiableMap(new LinkedHashMap<>(result));
    }

}
