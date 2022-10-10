package ru.practicum.ewm.error.handler.exception;

public class UserNotFoundException extends ObjectNotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
