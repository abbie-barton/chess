package ui;

public class ResponseException extends Exception {

    public ResponseException(int statusCode, String message) {
        super(message);
    }

}

