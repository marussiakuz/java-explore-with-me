package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.client.dto.ViewStatsDto;

import java.util.List;
import java.util.Map;

@Slf4j
public class StatClient {
    private final RestTemplate rest;

    public StatClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <T> boolean post(String path, T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, getHeaders());
        ResponseEntity<Object> response = rest.exchange(path, HttpMethod.POST, requestEntity, Object.class);
        log.info("the response to the POST request from the statistic server has status code={}",
                response.getStatusCodeValue());
        return response.getStatusCode().is2xxSuccessful();
    }

    protected List<ViewStatsDto> get(String path, Map<String, Object> parameters) {
        HttpEntity<List<ViewStatsDto>> requestEntity = new HttpEntity<>(getHeaders());

        ResponseEntity<List<ViewStatsDto>> response =
                rest.exchange(
                        path, HttpMethod.GET, requestEntity,
                        new ParameterizedTypeReference<>() {
                        }, parameters);

        log.info("the response to the GET request from the statistic server has status code={}",
                response.getStatusCodeValue());
        return response.getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
