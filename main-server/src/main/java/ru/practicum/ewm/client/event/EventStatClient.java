package ru.practicum.ewm.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.client.HitMapper;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.client.dto.HitDto;
import ru.practicum.ewm.client.dto.ViewStatsDto;
import ru.practicum.ewm.event.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventStatClient extends StatClient {
    private static final String API_POSTFIX_POST = "/hit";
    private static final String API_POSTFIX_GET = "/stats?start={start}&end={end}&unique={unique}";

    @Autowired
    public EventStatClient(@Value("stats-server-url") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public Map<Long, Long> getStatisticOnViews(List<Event> events, boolean unique) {
        return getViews(events, unique).stream()
                .filter(viewStatsDto -> viewStatsDto.getApp().equalsIgnoreCase("ewm-main-service"))
                .collect(Collectors.toMap(viewStatsDto -> getEventId(viewStatsDto.getUri()), ViewStatsDto::getHits));
    }

    public void sendViewToStatsServer(HttpServletRequest request) {
        HitDto body = HitMapper.requestToHit(request);
        boolean isSuccessful = post(API_POSTFIX_POST, body);
        log.info("viewing information was sent to statistic server:\nuri={}, \n\"the request {}", body.getUri(),
                isSuccessful ? "was sent successfully" : "returned with an error");
    }

    private List<ViewStatsDto> getViews(List<Event> events, boolean unique) {
        String[] urisParameters = events.stream()
                .map(this::mapEventToUriParameter)
                .toArray(String[]::new);

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> parameters = Map.of(
                "start", URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8),
                "end", URLEncoder.encode(LocalDateTime.now().format(formatter), StandardCharsets.UTF_8),
                "unique", unique
        );

        String finalUri = API_POSTFIX_GET + String.join("", urisParameters);

        return get(finalUri, parameters);
    }

    private String mapEventToUriParameter(Event event) {
        return "&uris=http://localhost:8080/events/" + event.getId();
    }

    private long getEventId(String uri) {
        String[] uriParts = uri.split("/");
        return Long.parseLong(uriParts[uriParts.length - 1]);
    }
}
