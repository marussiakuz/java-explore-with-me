package ru.practicum.ewm.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewWithHits {
    private String app;
    private String uri;
    private long hits;

    public ViewWithHits(String app, String uri, long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}
