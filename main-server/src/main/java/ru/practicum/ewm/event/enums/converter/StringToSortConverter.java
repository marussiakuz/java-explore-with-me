package ru.practicum.ewm.event.enums.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.error.handler.exception.InvalidRequestException;
import ru.practicum.ewm.event.enums.SortingEvents;

@Component
public class StringToSortConverter implements Converter<String, SortingEvents> {

    @Override
    public SortingEvents convert(String source) {
        try {
            return SortingEvents.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(String.format("an unexpected error occurred when converting string " +
                    "value=%s into SortingEvents", source));
        }
    }
}
