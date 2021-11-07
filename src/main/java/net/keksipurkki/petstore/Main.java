package net.keksipurkki.petstore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.http.HttpVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        var vertx = Vertx.vertx();
        vertx.deployVerticle(new Main(args));
    }

    public Main(String... args) {
        logger.info("Starting {} with args {}", Main.class, args);
    }

    @Override
    public void start(Promise<Void> promise) {

        vertx.executeBlocking(this::deployVerticles).onComplete(ar -> {

            if (ar.failed()) {
                promise.fail(ar.cause());
                logger.error("Deployment failed", ar.cause());
                abort(ar.cause());
                return;
            }

            logger.info("Deployment completed successfully");
            promise.complete();
        });
    }

    private void deployVerticles(Promise<Void> promise) {
        var future = Future.succeededFuture();
        var server = new HttpVerticle();
        future
            .flatMap(v -> vertx.deployVerticle(server))
            .flatMap(v -> server.listen(8080))
            .onComplete(ar -> {
                if (ar.failed()) {
                    promise.fail(ar.cause());
                } else {
                    var port = server.getPort();
                    logger.info("Server is now running at http://localhost:{}", port);
                    promise.complete();
                }
            });
    }

    private void abort(Throwable throwable) {
        vertx.close();
        System.exit(1);
    }

}
