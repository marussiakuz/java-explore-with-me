package ru.practicum.ewm.error.handler.exception;

public class EventNotFoundException extends ObjectNotFoundException {

    public EventNotFoundException(String message) {
        super(message);
    }
}
