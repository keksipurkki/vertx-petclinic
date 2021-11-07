package net.keksipurkki.petstore.support;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;

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

    /**
     * Perform arbitrary side effects on future value of type `T`
     * <p>
     * The side effects are executed in sequence and immediately after `future` is resolved
     * <p>
     * If any of the side effects throws an exception, the future fails with that exception.
     */
    @SafeVarargs
    public static <T> Future<T> sideEffect(Future<T> future, Handler<T>... handlers) {
        return future.map(input -> {
            for (var handler : handlers) {
                handler.handle(input);
            }
            return input;
        });
    }

    /**
     * Perform arbitrary asynchronous side effects on future value of type `T`
     * <p>
     * The effects are executed in parallel after `future` is resolved.
     * <p>
     * If any of the asynchronous side effects fails with an exception, the returned
     * future fails with that exception with the same semantics as `CompositeFuture::all`.
     */
    @SafeVarargs
    public static <T> Future<T> asyncSideEffect(Future<T> future, Function<T, Future<Void>>... handlers) {
        return future.flatMap(input -> {

            var sequence = new ArrayList<Future<Void>>();

            for (var handler : handlers) {
                sequence.add(handler.apply(input));
            }

            var join = sequence.stream().collect(Futures.collector());
            return join.map(v -> input);
        });
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
