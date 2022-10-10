package ru.practicum.ewm.util;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

public class StringArrayConverter extends SimpleArgumentConverter {
    @Override
    protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
        if (source instanceof String && String[].class.isAssignableFrom(targetType)) {
            return ((String) source).split("\\s*;\\s*");
        } else throw new IllegalArgumentException(String.format("Conversion from %s to %s not supported.",
                source.getClass(), targetType));
    }
}
