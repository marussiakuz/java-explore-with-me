package ru.practicum.ewm.service;

import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;

import java.util.List;

public interface StatService {

    void saveView(ViewInDto viewInDto);

    List<ViewOutDto> getStats(String start, String end, String[] uris, boolean unique);
}
