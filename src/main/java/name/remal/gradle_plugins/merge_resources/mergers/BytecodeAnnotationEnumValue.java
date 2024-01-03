package name.remal.gradle_plugins.merge_resources.mergers;

import static org.objectweb.asm.Type.getType;

import lombok.NonNull;
import lombok.Value;

@Value
class BytecodeAnnotationEnumValue {

    @NonNull
    String desc;

    @NonNull
    String value;

    @Override
    public String toString() {
        return getType(desc).getClassName() + '.' + value;
    }

}
