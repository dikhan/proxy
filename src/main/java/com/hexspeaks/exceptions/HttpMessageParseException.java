package com.hexspeaks.exceptions;

public class HttpMessageParseException extends Exception {

    public HttpMessageParseException(String message) {
        super(message);
    }

    public HttpMessageParseException(String message, Throwable ex) {
        super(message, ex);
    }

}
