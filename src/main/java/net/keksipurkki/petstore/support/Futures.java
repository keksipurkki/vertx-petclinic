package net.keksipurkki.petstore.support;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements higher order functions for composing `Future<T>` objects
 * <p>
 * Helps you to avoid callback hell and glitches related to `Future<T>`
 */
final public class Futures {

    private Futures() {
    }

    /**
     * Allows you to convert `List<Future<T>>` to `Future<List<T>>` using Java Stream API
     *
     * @see "https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collector.html"
     */
    public static <T> Collector<Future<T>, ?, Future<List<T>>> collector() {
        return new FutureCollector<T>();
    }

    /**
     * Allows you to concatenate arbitrary lists of futures on the same type `T` to a single `List<Future<T>>`
     */
    @SafeVarargs
    public static <T> List<Future<T>> concat(Collection<Future<T>>... futures) {
        return Stream.of(futures).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Prepend a future value of type `T` to a list of such future values
     */
    public static <T> List<Future<T>> prepend(Future<T> item, Collection<Future<T>> list) {
        return concat(Collections.singleton(item), list);
    }

    /**
     * Append a future value of type `T` to a list of such future values
     */
    public static <T> List<Future<T>> append(Collection<Future<T>> list, Future<T> item) {
        return concat(list, Collections.singleton(item));
    }

    private static class FutureCollector<T> implements Collector<Future<T>, List<Future<T>>, Future<List<T>>> {

        @Override
        public Supplier<List<Future<T>>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<Future<T>>, Future<T>> accumulator() {
            return List::add;
        }

        @Override
        public BinaryOperator<List<Future<T>>> combiner() {
            return (left, right) -> {
                left.addAll(right);
                return left;
            };
        }

        @Override
        @SuppressWarnings("rawtypes") // see e.g. https://github.com/eclipse-vertx/vert.x/pull/1994
        public Function<List<Future<T>>, Future<List<T>>> finisher() {
            return (list) -> {
                List<Future> workaround = new ArrayList<>(list);
                return CompositeFuture.all(workaround).map(CompositeFuture::list);
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

}
