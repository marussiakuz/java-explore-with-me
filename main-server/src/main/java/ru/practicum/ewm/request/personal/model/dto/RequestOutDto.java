package ru.practicum.ewm.request.personal.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.enums.Status;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestOutDto {

    private Long id;
    private int event;
    private LocalDateTime created;
    private int requester;
    private Status status;
}
