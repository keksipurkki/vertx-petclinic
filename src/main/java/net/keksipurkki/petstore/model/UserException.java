package net.keksipurkki.petstore.model;

import net.keksipurkki.petstore.api.ApiException;

public class UserException extends ApiException {

    public UserException(String message) {
        super(message);
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
