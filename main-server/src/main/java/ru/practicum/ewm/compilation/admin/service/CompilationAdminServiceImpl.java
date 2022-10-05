package ru.practicum.ewm.compilation.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.client.event.StatisticEventService;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationInDto;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.model.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.error.handler.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompilationAdminServiceImpl extends StatisticEventService implements CompilationAdminService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CompilationAdminServiceImpl(EventStatClient eventStatClient, RequestRepository requestRepository,
                                       CompilationRepository compilationRepository, EventRepository eventRepository) {
        super(eventStatClient, requestRepository);
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public CompilationOutDto createCompilation(CompilationInDto compilationInDto) {
        List<Event> events = Arrays.stream(compilationInDto.getEvents())
                .mapToObj(id -> eventRepository.findById(id)
                        .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", id))))
                .collect(Collectors.toList());

        Compilation compilation = CompilationMapper.toCompilation(compilationInDto, events);
        Compilation saved = compilationRepository.save(compilation);
        log.info("new compilation id={}, events count={} successfully added", saved.getId(),
                compilation.getEvents().size());

        return CompilationMapper.toCompilationOut(saved, addConfirmedRequestsAndViews(events, false).stream()
                .map(eventOutDto -> (EventShortOutDto) eventOutDto)
                .collect(Collectors.toList()));
    }

    @Override
    public void deleteCompilation(long compId) {
        if(!compilationRepository.existsById(compId))
            throw new CompilationNotFoundException(String.format("Compilation with id=%s not found", compId));

        compilationRepository.deleteById(compId);
        log.info("compilation id={} successfully deleted", compId);
    }

    @Override
    public void deleteEvent(long compId, long eventId) {
        Compilation compilation = getCompilationById(compId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", eventId)));

        List<Event> events = new ArrayList<>(compilation.getEvents());
        events.remove(event);
        compilation.setEvents(events);

        compilationRepository.save(compilation);
        log.info("event id={} successfully deleted from compilation id={}", eventId, compId);
    }

    @Override
    public void addEvent(long compId, long eventId) {
        Compilation compilation = getCompilationById(compId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id=%s not found", eventId)));

        List<Event> events = new ArrayList<>(compilation.getEvents());
        events.add(event);
        compilation.setEvents(events);

        compilationRepository.save(compilation);
        log.info("event id={} successfully added to compilation id={}", eventId, compId);
    }

    @Override
    public void unpinCompilation(long compId) {
        changePinned(compId, false);
        log.info("compilation id={} successfully unpinned", compId);
    }

    @Override
    public void pinCompilation(long compId) {
        changePinned(compId, true);
        log.info("compilation id={} successfully pinned", compId);
    }

    private void changePinned(long compId, boolean pinned) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(String.format("Compilation with id=%s not found",
                        compId)));

        compilation.setPinned(pinned);

        compilationRepository.save(compilation);
    }

    private Compilation getCompilationById(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(String.format("Compilation with id=%s not found",
                        compId)));
    }
}
