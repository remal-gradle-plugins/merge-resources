package name.remal.gradle_plugins.merge_resources.mergers;

import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
abstract class Utils {

    public static <E> E removeFirst(Iterable<E> iterable) {
        val iterator = iterable.iterator();
        val firstElement = iterator.next();
        iterator.remove();
        return firstElement;
    }

    public static <E, V> Predicate<E> distinctBy(Function<E, V> getter) {
        val processed = new LinkedHashSet<V>();
        return element -> {
            val value = getter.apply(element);
            return processed.add(value);
        };
    }

}
