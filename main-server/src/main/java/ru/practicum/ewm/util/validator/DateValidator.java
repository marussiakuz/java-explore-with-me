package ru.practicum.ewm.util.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class DateValidator implements ConstraintValidator<IsLaterFromTheCurrentTime, LocalDateTime> {
    private LocalDateTime minDateTime;
    private boolean isNullable;

    @Override
    public void initialize(IsLaterFromTheCurrentTime constraintAnnotation) {
        int days = constraintAnnotation.days();
        int hours = constraintAnnotation.hours();
        int minutes = constraintAnnotation.minutes();
        isNullable = constraintAnnotation.isNullable();
        minDateTime = LocalDateTime.now().plusDays(days).plusHours(hours).plusMinutes(minutes);
    }

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext constraintValidatorContext) {
        if(isNullable && localDateTime == null) return true;
        assert localDateTime != null;
        return localDateTime.isAfter(minDateTime);
    }
}
