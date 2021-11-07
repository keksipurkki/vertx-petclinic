package net.keksipurkki.petstore.http;

import net.keksipurkki.petstore.api.ApiException;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 403;
    }

    @Override
    public String getDetail() {
        return null;
    }
}
