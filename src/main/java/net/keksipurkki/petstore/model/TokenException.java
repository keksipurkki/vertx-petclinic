package net.keksipurkki.petstore.model;

import net.keksipurkki.petstore.api.ApiException;

public class TokenException extends ApiException {

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }

    @Override
    public String getDetail() {
        return getMessage(); // Do not yield details
    }
}
