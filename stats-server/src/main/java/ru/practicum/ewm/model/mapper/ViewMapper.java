package ru.practicum.ewm.model.mapper;

import ru.practicum.ewm.model.View;
import ru.practicum.ewm.model.ViewWithHits;
import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;

public class ViewMapper {

    public static View toView(ViewInDto view) {
        return View.builder()
                .app(view.getApp())
                .ip(view.getIp())
                .uri(view.getUri())
                .timestamp(view.getTimestamp())
                .build();
    }

    public static ViewOutDto toViewOut(ViewWithHits view) {
        return ViewOutDto.builder()
                .app(view.getApp())
                .uri(view.getUri())
                .hits(view.getHits())
                .build();
    }
}
