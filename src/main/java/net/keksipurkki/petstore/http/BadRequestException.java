package net.keksipurkki.petstore.http;

import net.keksipurkki.petstore.api.ApiException;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message, null);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }

    @Override
    public String getDetail() {
        return getMessage();
    }
}
