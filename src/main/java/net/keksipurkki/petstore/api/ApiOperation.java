package net.keksipurkki.petstore.api;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import net.keksipurkki.petstore.model.UserData;
import net.keksipurkki.petstore.security.SecurityContext;
import net.keksipurkki.petstore.security.SecurityScheme;
import net.keksipurkki.petstore.support.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Objects.nonNull;

/**
 * Front controller of the API
 */
public enum ApiOperation implements Handler<RoutingContext> {

    CREATE_USER,
    CREATE_USER_LIST,
    GET_USER_BY_NAME,
    UPDATE_USER,
    DELETE_USER,
    LOGIN_USER,
    LOGOUT_USER,
    ;

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
        };

        operation.onSuccess(rc::json).onFailure(rc::fail).onComplete(ar -> {
            if (ar.failed()) {
                logger.warn("Execution of {} failed", this);
            } else {
                logger.info("Execution of {} succeeded", this);
            }
        });

    }

    public SecurityScheme getSecurityScheme() {
        return switch (this) {
            case CREATE_USER, CREATE_USER_LIST, GET_USER_BY_NAME -> SecurityScheme.NONE;
            case LOGIN_USER, LOGOUT_USER -> SecurityScheme.NONE;
            case UPDATE_USER, DELETE_USER -> SecurityScheme.LOGIN_SESSION;
        };
    }


    private UserData userRecord(RequestParameters params) {
        return Json.parse(params.body().getJsonObject(), UserData.class);
    }

    private List<UserData> userRecordList(RequestParameters params) {
        return Json.parse(params.body().getJsonArray(), UserData[].class);
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
