package ru.practicum.ewm.event.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Sort {

    EVENT_DATE("EVENT_DATE"),
    VIEWS("VIEWS");

    private final String sort;
}
