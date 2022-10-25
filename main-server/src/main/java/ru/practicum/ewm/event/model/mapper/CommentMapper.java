package ru.practicum.ewm.event.model.mapper;

import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.dto.CommentInDto;
import ru.practicum.ewm.event.model.dto.CommentOutDto;

public class CommentMapper {
    public static Comment toComment(CommentInDto commentIn, Event event) {
        return Comment.builder()
                .text(commentIn.getText())
                .event(event)
                .closed(false)
                .build();
    }

    public static CommentOutDto toCommentOut(Comment comment) {
        return CommentOutDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }
}
