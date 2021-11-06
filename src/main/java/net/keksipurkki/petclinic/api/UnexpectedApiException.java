package net.keksipurkki.petclinic.api;

import java.net.URI;

public class UnexpectedApiException extends ApiException {

    public UnexpectedApiException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return 500;
    }

    @Override
    public String getDetail() {
        return getMessage();
    }

}
