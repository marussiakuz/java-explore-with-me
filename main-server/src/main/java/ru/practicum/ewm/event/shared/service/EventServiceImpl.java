package ru.practicum.ewm.event.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.model.dto.EventFullOutDto;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;

import java.util.List;

@Service
@Slf4j
public class EventServiceImpl implements EventService {
    @Override
    public List<EventShortOutDto> getEvents(String text, int[] categories, boolean paid, String rangeStart, String rangeEnd,
                                            boolean onlyAvailable, String sort, int from, int size) {
        return null;
    }

    @Override
    public EventFullOutDto getEventById(long id) {
        return null;
    }
}
