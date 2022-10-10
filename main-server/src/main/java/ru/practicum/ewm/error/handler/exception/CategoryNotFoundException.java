package ru.practicum.ewm.error.handler.exception;

public class CategoryNotFoundException extends ObjectNotFoundException {

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
