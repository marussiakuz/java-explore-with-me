package ru.practicum.ewm.error.handler.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
        log.error(message);
    }
}
