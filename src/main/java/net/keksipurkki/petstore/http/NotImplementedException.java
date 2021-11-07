package net.keksipurkki.petstore.http;

import net.keksipurkki.petstore.api.ApiException;

public class NotImplementedException extends ApiException {
    public NotImplementedException() {
        super("Not implemented");
    }

    @Override
    public int getStatusCode() {
        return 501;
    }

    @Override
    public String getDetail() {
        return "The operation has not been implemented";
    }
}
