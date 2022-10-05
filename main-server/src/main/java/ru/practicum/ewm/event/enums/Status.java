package ru.practicum.ewm.event.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    REJECTED("REJECTED"),
    CANCELED("CANCELED");

    private final String status;
}
