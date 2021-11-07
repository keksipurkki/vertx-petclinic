package it;

import io.vertx.core.Future;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;

import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

public final class Tests {
    private Tests() {
    }

    @SneakyThrows
    public static int randomPort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public static <T> T await(Future<T> future) {
        try {
            return future.toCompletionStage().toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException cause) {
            Assertions.fail(cause);
            return null;
        }
    }

}
