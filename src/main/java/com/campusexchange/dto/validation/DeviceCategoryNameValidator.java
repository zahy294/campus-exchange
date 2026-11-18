package com.campusexchange.dto.validation;

import com.campusexchange.model.DeviceCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Checks membership in {@link DeviceCategory}; the error lists the current categories. */
public class DeviceCategoryNameValidator implements ConstraintValidator<DeviceCategoryName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank() || DeviceCategory.isKnown(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Category must be one of " + DeviceCategory.allNames())
                .addConstraintViolation();
        return false;
    }
}
