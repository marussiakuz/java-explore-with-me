package ru.practicum.ewm.error.handler.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoAccessRightsException extends RuntimeException {

    public NoAccessRightsException(String message) {
        super(message);
        log.error(message);
    }
}
