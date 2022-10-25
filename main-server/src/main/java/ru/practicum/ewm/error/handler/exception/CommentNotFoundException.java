package ru.practicum.ewm.error.handler.exception;

public class CommentNotFoundException extends ObjectNotFoundException {

    public CommentNotFoundException(String message) {
        super(message);
    }
}
