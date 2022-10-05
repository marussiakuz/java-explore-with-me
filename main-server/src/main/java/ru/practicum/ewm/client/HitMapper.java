package ru.practicum.ewm.client;

import ru.practicum.ewm.client.dto.HitDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

public class HitMapper {

    public static HitDto requestToHit(HttpServletRequest request) {
        return HitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
    }

}
