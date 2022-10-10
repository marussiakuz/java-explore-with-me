package ru.practicum.ewm.error.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.error.handler.exception.*;

import javax.validation.ConstraintViolationException;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({DuplicateObjectException.class, ConditionIsNotMetException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationException(final RuntimeException e) {
        String reason = e.getClass() == ConditionIsNotMetException.class ?
                "For the requested operation the conditions are not met." : "It isn't possible to re-place the object";
        return new ErrorResponse(e.getMessage(), reason, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({ObjectNotFoundException.class, HttpMediaTypeNotAcceptableException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        String reason = e instanceof ObjectNotFoundException ? "The required object was not found."
                : "HttpMediaType not accept";
        return new ErrorResponse(e.getMessage(), reason, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoAccessRightsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNoAccessRightsException(final RuntimeException e) {
        return new ErrorResponse(e.getMessage(), "Insufficient access rights", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({InvalidRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequestException(final RuntimeException e) {
        return new ErrorResponse(e.getMessage(), "The request was made with errors", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ErrorResponse(Objects.requireNonNull(e.getFieldError()).getDefaultMessage(),
                "Field error in object", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream().findFirst().isPresent() ?
                e.getConstraintViolations().stream().findFirst().get().getMessage() : e.getMessage();
        return new ErrorResponse(message, "Error in URI parameters", HttpStatus.BAD_REQUEST);
    }
}
