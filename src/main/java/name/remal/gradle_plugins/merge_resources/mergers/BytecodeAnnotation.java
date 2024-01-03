package name.remal.gradle_plugins.merge_resources.mergers;

import static java.util.stream.Collectors.joining;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeJava;
import static org.objectweb.asm.Type.getType;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import org.objectweb.asm.Type;

@Value
class BytecodeAnnotation {

    @NonNull
    String desc;

    @NonNull
    Map<String, Object> values;

    @Override
    public String toString() {
        return '@'
            + getType(desc).getClassName()
            + values.entrySet().stream()
            .map(entry -> entry.getKey() + " = " + valueToString(entry.getValue()))
            .collect(joining(", ", "(", ")"));
    }

    private static String valueToString(Object value) {
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                .map(BytecodeAnnotation::valueToString)
                .collect(joining(", ", "{", "}"));

        } else if (value instanceof String) {
            return '"' + escapeJava("" + value) + '"';

        } else if (value instanceof Character) {
            return '\'' + escapeJava("" + value) + '\'';

        } else if (value instanceof Type) {
            return ((Type) value).getClassName();

        } else {
            return value.toString();
        }
    }

}
