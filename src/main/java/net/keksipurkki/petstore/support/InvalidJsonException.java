package net.keksipurkki.petstore.support;

import net.keksipurkki.petstore.api.ApiException;

public class InvalidJsonException extends ApiException {
    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return 500;
    }

    @Override
    public String getDetail() {
        return getCause().getMessage();
    }

}
