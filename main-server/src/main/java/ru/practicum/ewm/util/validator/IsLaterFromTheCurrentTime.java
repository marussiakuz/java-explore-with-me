package ru.practicum.ewm.util.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = DateValidator.class)
@Documented
public @interface IsLaterFromTheCurrentTime {
    String message() default "The date and time don't comply the rules";

    int days() default 0;

    int hours() default 2;

    int minutes() default 0;
    boolean isNullable() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
