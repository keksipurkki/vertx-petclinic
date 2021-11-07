package net.keksipurkki.petstore.api;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import net.keksipurkki.petstore.model.UserRecord;
import net.keksipurkki.petstore.support.Json;

import java.util.List;

public enum ApiOperation implements Handler<RoutingContext> {

    CREATE_USER,
    CREATE_USER_LIST,
    GET_USER_BY_NAME,
    UPDATE_USER,
    DELETE_USER,
    LOGIN_USER,
    LOGOUT_USER,
    ;

    private Api prototype;

    @Override
    public void handle(RoutingContext rc) {
        var requestParams = rc.<RequestParameters>get(ValidationHandler.REQUEST_CONTEXT_KEY);
        handle(rc, requestParams);
    }

    private void handle(RoutingContext rc, RequestParameters params) {
        var api = prototype;
        var operation = switch (this) {
            case CREATE_USER -> api.createUser(userRecord(params));
            case CREATE_USER_LIST -> api.createWithList(userRecordList(params));
            case GET_USER_BY_NAME -> api.getUserByName(usernameFromPath(params));
            case UPDATE_USER -> api.updateUser(usernameFromPath(params), userRecord(params));
            case DELETE_USER -> api.deleteUser(usernameFromPath(params));
            case LOGIN_USER -> api.login(queryParameter(params, "username"), queryParameter(params, "password"));
            case LOGOUT_USER -> api.logout(null);
        };
        operation.onSuccess(rc::json).onFailure(rc::fail);
    }

    private UserRecord userRecord(RequestParameters params) {
        return Json.parse(params.body().getJsonObject(), UserRecord.class);
    }

    private List<UserRecord> userRecordList(RequestParameters params) {
        return Json.parse(params.body().getJsonArray(), UserRecord[].class);
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

    @FunctionalInterface
    public interface ApiOperationHandler<T> {
        Future<T> handle(Api api);
    }

}
