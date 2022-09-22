package ru.practicum.ewm.event.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {

    REJECTED("REJECTED"),
    PENDING("PENDING"),
    PUBLISHED("PUBLISHED"),
    CANCELLED("CANCELLED");

    private final String status;
}
