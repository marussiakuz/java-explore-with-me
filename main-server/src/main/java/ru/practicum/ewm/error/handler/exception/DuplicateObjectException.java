package ru.practicum.ewm.error.handler.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DuplicateObjectException extends RuntimeException {

    public DuplicateObjectException(String message) {
        super(message);
        log.error(message);
    }
}
