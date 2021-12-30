package net.keksipurkki.petstore.api;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import net.keksipurkki.petstore.security.SecurityContext;
import net.keksipurkki.petstore.security.SecurityScheme;
import net.keksipurkki.petstore.store.NewOrder;
import net.keksipurkki.petstore.store.Order;
import net.keksipurkki.petstore.support.Json;
import net.keksipurkki.petstore.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Objects.nonNull;

/**
 * API Front Controller
 */
public enum ApiOperation implements Handler<RoutingContext> {

    CREATE_USER,
    CREATE_USER_LIST,
    GET_USER_BY_NAME,
    UPDATE_USER,
    DELETE_USER,
    LOGIN_USER,
    LOGOUT_USER,

    GET_INVENTORY,
    PLACE_ORDER,
    GET_ORDER,
    DELETE_ORDER;

    private final static Logger logger = LoggerFactory.getLogger(ApiOperation.class);
    private Api prototype;

    @Override
    public void handle(RoutingContext rc) {
        var requestParams = rc.<RequestParameters>get(ValidationHandler.REQUEST_CONTEXT_KEY);
        var securityContext = rc.<SecurityContext>get(SecurityContext.REQUEST_CONTEXT_KEY);
        handle(rc, securityContext, requestParams);
    }

    private void handle(RoutingContext rc, SecurityContext sc, RequestParameters params) {

        logger.info("Executing {} under security scheme {}. Security context defined: {}", this, getSecurityScheme(), nonNull(sc));

        // The Api instance must be scoped per API operation. Security context is null for anonymous routes
        var api = prototype.withSecurityContext(sc);

        var operation = switch (this) {
            case CREATE_USER -> api.createUser(userRecord(params));
            case CREATE_USER_LIST -> api.createWithList(userRecordList(params));
            case GET_USER_BY_NAME -> api.getUserByName(usernameFromPath(params));
            case UPDATE_USER -> api.updateUser(usernameFromPath(params), userRecord(params));
            case DELETE_USER -> api.deleteUser(usernameFromPath(params));
            case LOGIN_USER -> api.login(queryParameter(params, "username"), queryParameter(params, "password"));
            case LOGOUT_USER -> api.logout();
            case GET_INVENTORY -> api.getInventory();
            case PLACE_ORDER -> api.placeOrder(newOrderRecord(params));
            case GET_ORDER -> api.getOrderById(orderId(params));
            case DELETE_ORDER -> api.deleteOrder(orderId(params));
        };

        operation.onSuccess(respond(rc)).onFailure(rc::fail).onComplete(ar -> {
            if (ar.failed()) {
                logger.warn("Execution of {} failed", this);
            } else {
                logger.info("Execution of {} succeeded", this);
            }
        });

    }

    private int orderId(RequestParameters params) {
        return params.pathParameter("orderId").getInteger();
    }

    private NewOrder newOrderRecord(RequestParameters params) {
        return Json.parse(params.body().getJsonObject(), NewOrder.class);
    }

    private <T> Handler<T> respond(RoutingContext rc) {
        return value -> {
            rc.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.stringify(value, true));
        };
    }

    public SecurityScheme getSecurityScheme() {
        return switch (this) {

            // User operations
            case CREATE_USER, CREATE_USER_LIST, GET_USER_BY_NAME -> SecurityScheme.NONE;
            case LOGIN_USER, LOGOUT_USER -> SecurityScheme.NONE;
            case UPDATE_USER, DELETE_USER -> SecurityScheme.LOGIN_SESSION;

            // Store operations
            case GET_INVENTORY -> SecurityScheme.NONE;
            case PLACE_ORDER, GET_ORDER, DELETE_ORDER -> SecurityScheme.LOGIN_SESSION;
        };
    }

    private User userRecord(RequestParameters params) {
        return Json.parse(params.body().getJsonObject(), User.class);
    }

    private List<User> userRecordList(RequestParameters params) {
        return Json.parse(params.body().getJsonArray(), User[].class);
    }

    private String usernameFromPath(RequestParameters params) {
        return params.pathParameter("username").getString();
    }

    private String queryParameter(RequestParameters params, String parameter) {
        return params.queryParameter(parameter).getString();
    }

    public static ApiOperation from(String operationId) {
        try {
            return ApiOperation.valueOf(operationId);
        } catch (IllegalArgumentException cause) {
            throw new UnexpectedApiException("Unsupported API operation " + operationId, cause);
        }
    }

    public ApiOperation withApi(Api prototype) {
        this.prototype = prototype;
        return this;
    }

}
