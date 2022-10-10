package ru.practicum.ewm.error.handler.exception;

public class RequestNotFoundException extends ObjectNotFoundException {

    public RequestNotFoundException(String message) {
        super(message);
    }
}
