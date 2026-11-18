package com.campusexchange.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The value must name a {@link com.campusexchange.model.DeviceCategory} (case-insensitive).
 * Null and blank values pass; pair with {@code @NotBlank} to require one.
 */
@Documented
@Constraint(validatedBy = DeviceCategoryNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeviceCategoryName {
    String message() default "Choose one of the listed categories";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
