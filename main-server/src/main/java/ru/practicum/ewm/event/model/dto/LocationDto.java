package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private float latitude;
    private float longitude;

    @JsonProperty(value = "lat")
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    @JsonProperty(value = "lon")
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
