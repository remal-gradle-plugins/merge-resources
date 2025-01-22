package name.remal.gradle_plugins.merge_resources.mergers;

import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
abstract class Utils {

    public static <E> E removeFirst(Iterable<E> iterable) {
        var iterator = iterable.iterator();
        var firstElement = iterator.next();
        iterator.remove();
        return firstElement;
    }

    public static <E, V> Predicate<E> distinctBy(Function<E, V> getter) {
        var processed = new LinkedHashSet<V>();
        return element -> {
            var value = getter.apply(element);
            return processed.add(value);
        };
    }

}
