package ru.practicum.ewm.event.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {
    REJECTED("REJECTED"),
    PENDING("PENDING"),
    RE_MODERATION("RE_MODERATION"),
    PUBLISHED("PUBLISHED"),
    CANCELED("CANCELED");

    private final String state;
}
