package ru.practicum.ewm.event.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortingEvents {
    EVENT_DATE("EVENT_DATE"),
    VIEWS("VIEWS");

    private final String sort;
}
