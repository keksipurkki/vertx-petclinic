package net.keksipurkki.petstore.http;

import net.keksipurkki.petstore.api.ApiException;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message) {
        super(message, null);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }

    @Override
    public String getDetail() {
        return "Unauthorized"; // Do not reveal any details
    }
}
