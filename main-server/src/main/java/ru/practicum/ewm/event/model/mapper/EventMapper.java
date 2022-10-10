package ru.practicum.ewm.event.model.mapper;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.mapper.CategoryMapper;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.*;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.mapper.UserMapper;

import java.time.LocalDateTime;

public class EventMapper {
    public static EventShortOutDto toEventShort(Event event, int confirmedRequests, long views) {
        return EventShortOutDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .title(event.getTitle())
                .initiator(UserMapper.toUserShort(event.getInitiator()))
                .category(CategoryMapper.toCategoryOut(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }

    public static EventFullOutDto toEventFull(Event event, int confirmedRequests, long views) {
        return getEventFullBuilder(event, confirmedRequests, views).build();
    }

    public static EventCommentedDto toEventCommented(Event event, Comment comment) {
        return new EventCommentedDto(getEventFullBuilder(event, 0, 0),
                CommentMapper.toCommentOut(comment));
    }

    public static Event toEvent(EventInDto eventInDto, Category category, User user) {
        return Event.builder()
                .category(category)
                .eventDate(eventInDto.getEventDate())
                .annotation(eventInDto.getAnnotation())
                .createdOn(LocalDateTime.now())
                .initiator(user)
                .description(eventInDto.getDescription())
                .participantLimit(eventInDto.getParticipantLimit())
                .locationLatitude(eventInDto.getLocation().getLatitude())
                .locationLongitude(eventInDto.getLocation().getLongitude())
                .state(State.PENDING)
                .title(eventInDto.getTitle())
                .requestModeration(eventInDto.isRequestModeration())
                .paid(eventInDto.isPaid())
                .build();
    }

    private static LocationDto getLocationFromEvent(Event event) {
        return LocationDto.builder()
                .latitude(event.getLocationLatitude())
                .longitude(event.getLocationLongitude())
                .build();
    }

    private static EventFullOutDto.EventFullOutDtoBuilder<?, ?> getEventFullBuilder(Event event, int confirmedRequests,
                                                                                    long views) {
        return EventFullOutDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .title(event.getTitle())
                .initiator(UserMapper.toUserShort(event.getInitiator()))
                .category(CategoryMapper.toCategoryOut(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .views(views)
                .location(getLocationFromEvent(event))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .state(event.getState());
    }
}
