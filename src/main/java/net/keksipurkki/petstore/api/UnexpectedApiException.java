package net.keksipurkki.petstore.api;

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
