package ru.practicum.ewm.event.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    RENDING("RENDING"),
    CONFIRMED("CONFIRMED"),
    REJECTED("REJECTED");

    private final String status;
}
