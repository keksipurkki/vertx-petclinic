package net.keksipurkki.petclinic.support;

import net.keksipurkki.petclinic.api.ApiException;

import java.net.URI;

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
