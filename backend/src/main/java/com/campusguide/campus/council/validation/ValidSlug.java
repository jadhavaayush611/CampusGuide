package com.campusguide.campus.council.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SlugValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSlug {

    String message() default "Slug must be URL-safe (lowercase letters, numbers, and hyphens only)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
