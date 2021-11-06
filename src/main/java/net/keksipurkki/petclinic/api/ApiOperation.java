package net.keksipurkki.petclinic.api;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public enum ApiOperation implements Handler<RoutingContext> {

    HELLO(api -> api.greet()),
    MEET(api -> api.meet());

    ApiOperation(ApiOperationHandler<?> handler) {
        this.handler = handler;
    }

    private final ApiOperationHandler<?> handler;

    @Override
    public void handle(RoutingContext rc) {
        var future = this.handler.handle(new Api());
        future.onSuccess(rc::json).onFailure(rc::fail);
    }

    public static ApiOperation from(String operationId) {
        try {
            return ApiOperation.valueOf(operationId);
        } catch (IllegalArgumentException cause) {
            throw new UnexpectedApiException("Unsupported API operation " + operationId, cause);
        }
    }

    @FunctionalInterface
    public interface ApiOperationHandler<T> {
        Future<T> handle(Api api);
    }

}
