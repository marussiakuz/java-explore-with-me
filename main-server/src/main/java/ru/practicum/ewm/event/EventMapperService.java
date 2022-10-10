package ru.practicum.ewm.event;

import ru.practicum.ewm.client.event.EventStatClient;
import ru.practicum.ewm.client.event.StatisticEventService;
import ru.practicum.ewm.error.handler.exception.CommentNotFoundException;
import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.EventOutDto;
import ru.practicum.ewm.event.model.mapper.EventMapper;
import ru.practicum.ewm.event.repository.CommentRepository;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.util.List;
import java.util.Optional;

public abstract class EventMapperService extends StatisticEventService {
    protected final CommentRepository commentRepository;

    public EventMapperService(EventStatClient eventStatClient, RequestRepository requestRepository,
                              CommentRepository commentRepository) {
        super(eventStatClient, requestRepository);
        this.commentRepository = commentRepository;
    }

    public EventOutDto mapToSuitableDtoDependingOnState(Event event) {
        switch (event.getState()) {
            case RE_MODERATION:
            case REJECTED:
                Comment comment = commentRepository.findByEventIdAndClosedIsFalse(event.getId())
                        .orElseThrow(() -> new CommentNotFoundException(String.format("No comments found for event " +
                                "id=%s, the current state of the event: %s", event.getId(), event.getState().name())));
                return EventMapper.toEventCommented(event, comment);
            case CANCELED:
                Optional<Comment> commentOptional = commentRepository.findByEventIdAndClosedIsFalse(event.getId());
                if (commentOptional.isPresent()) return EventMapper.toEventCommented(event, commentOptional.get());
            case PENDING:
                return EventMapper.toEventFull(event, 0, 0);
            default:
                return addConfirmedRequestsAndViews(List.of(event), true).get(0);
        }
    }
}
