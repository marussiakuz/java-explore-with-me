package ru.practicum.ewm.compilation.shared.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.client.event.StatisticEventService;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationOutDto;
import ru.practicum.ewm.compilation.model.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.error.handler.exception.CompilationNotFoundException;
import ru.practicum.ewm.event.model.dto.EventShortOutDto;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.util.Pagination;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompilationServiceImpl extends StatisticEventService implements CompilationService {
    private final CompilationRepository compilationRepository;

    @Autowired
    public CompilationServiceImpl(EventStatClient eventStatClient, RequestRepository requestRepository,
                                  CompilationRepository compilationRepository) {
        super(eventStatClient, requestRepository);
        this.compilationRepository = compilationRepository;
    }

    @Override
    public List<CompilationOutDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = Pagination.of(from, size);

        return mapToCompilationOut(pinned == null ? compilationRepository.findAll(pageable).getContent()
                : pinned ? compilationRepository.findAllByPinnedTrue(pageable).getContent()
                : compilationRepository.findAllByPinnedFalse(pageable).getContent());
    }

    @Override
    public CompilationOutDto getCompilationById(long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new CompilationNotFoundException(String.format("Compilation with id=%s not found",
                        compilationId)));

        return CompilationMapper.toCompilationOut(compilation,
                addConfirmedRequestsAndViews(compilation.getEvents(), false).stream()
                .map(eventOutDto -> (EventShortOutDto) eventOutDto)
                .collect(Collectors.toList()));
    }

    private List<CompilationOutDto> mapToCompilationOut(List<Compilation> compilations) {
        return compilations.stream()
                .map(compilation -> CompilationMapper.toCompilationOut(compilation,
                        addConfirmedRequestsAndViews(compilation.getEvents(), false).stream()
                                .map(eventOutDto -> (EventShortOutDto) eventOutDto)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
