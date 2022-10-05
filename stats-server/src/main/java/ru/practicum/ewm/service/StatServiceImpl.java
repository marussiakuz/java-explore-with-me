package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.UriParamDecodingException;
import ru.practicum.ewm.model.View;
import ru.practicum.ewm.model.dto.ViewInDto;
import ru.practicum.ewm.model.dto.ViewOutDto;
import ru.practicum.ewm.model.mapper.ViewMapper;
import ru.practicum.ewm.repo.StatRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {
    private final static DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatRepository statRepository;

    @Override
    public void saveView(ViewInDto viewInDto) {
        View view = statRepository.save(ViewMapper.toView(viewInDto));
        log.info("new view added: id={}, uri={}", view.getId(), view.getUri());
    }

    @Override
    public List<ViewOutDto> getStats(String start, String end, String[] uris, boolean unique) {
        if(uris == null) return new ArrayList<>();

        LocalDateTime finalStartDateTime = mapToLocalDateTime(start);
        LocalDateTime finalEndDateTime = mapToLocalDateTime(end);

        List<ViewOutDto> views = Arrays.stream(uris)
                .map(uri -> statRepository.getViewWithHits(finalStartDateTime, finalEndDateTime, uri, unique))
                .flatMap(Collection::stream)
                .map(ViewMapper::toViewOut)
                .toList();

        log.info("request for statistic of views successfully processed, views list size={}", views.size());
        return views;
    }

    private LocalDateTime mapToLocalDateTime(String encoded) {
        if(encoded == null) return null;

        String decoded;
        try {
            decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("the following parameter could not be decoded: {}", encoded);
            throw new UriParamDecodingException("there is a problem with decoding the date time parameter");
        }
        log.debug("decoding of the date time parameter has been successfully completed");

        return LocalDateTime.parse(decoded, DATE_TIME);
    }
}
