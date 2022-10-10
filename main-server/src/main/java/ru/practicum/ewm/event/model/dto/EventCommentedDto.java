package ru.practicum.ewm.event.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
public class EventCommentedDto extends EventFullOutDto {
    CommentOutDto comment;

    public EventCommentedDto(EventFullOutDtoBuilder<?, ?> b, CommentOutDto comment) {
        super(b);
        this.comment = comment;
    }
}
