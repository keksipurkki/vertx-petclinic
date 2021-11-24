package net.keksipurkki.petstore.http;

import net.keksipurkki.petstore.api.ApiException;

public class NotFoundException extends ApiException {

    public NotFoundException() {
        super("Not Found");
    }

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 404;
    }

    @Override
    public String getDetail() {
        return getMessage();
    }
}
