package ru.practicum.ewm.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.event.enums.State;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@SuperBuilder
public class EventFullOutDto extends EventOutDto {
    private LocationDto location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private String description;
    private int participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private boolean requestModeration;
    private State state;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventFullOutDto)) return false;
        if (!super.equals(o)) return false;
        EventFullOutDto that = (EventFullOutDto) o;
        return getParticipantLimit() == that.getParticipantLimit() && isRequestModeration() == that.isRequestModeration()
                && getLocation().equals(that.getLocation()) && getCreatedOn().equals(that.getCreatedOn())
                && getDescription().equals(that.getDescription()) && Objects.equals(getPublishedOn(),
                that.getPublishedOn()) && getState() == that.getState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getLocation(), getCreatedOn(), getDescription(), getParticipantLimit(),
                getPublishedOn(), isRequestModeration(), getState());
    }
}
