package ru.practicum.ewm.util;

import java.util.Random;
import java.util.stream.IntStream;

public interface TextProcessing {

    default String createText(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, length).forEach(iteration -> {
            char letter = (char) random.nextInt(Character.MAX_VALUE + 1);
            builder.append(letter);
        });

        return builder.toString();
    }
}
