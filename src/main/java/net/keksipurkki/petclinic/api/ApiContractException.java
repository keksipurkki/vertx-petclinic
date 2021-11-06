package net.keksipurkki.petclinic.api;

import io.vertx.ext.web.validation.BadRequestException;

import java.net.URI;

/**
 * Thrown if a request does not satisfy the OpenAPI contract
 * <p>
 * Translates Vert.x framework validation exceptions to ApiException hierarchy
 */
public class ApiContractException extends ApiException {

    public ApiContractException(String message, BadRequestException cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }

    @Override
    public String getDetail() {
        var cause = (BadRequestException) getCause();
        return cause.getMessage();
    }

}
