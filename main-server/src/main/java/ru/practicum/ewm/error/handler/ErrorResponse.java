package ru.practicum.ewm.error.handler;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorResponse {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private List<String> errors = new ArrayList<>();
    private String message;
    private String reason;
    private HttpStatus status;
    private String timestamp;

    public ErrorResponse(String message, String reason, HttpStatus status) {
        this.message = message;
        this.reason = reason;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(DATE_TIME);
    }
}
