package com.holink.validation;

import com.holink.validation.util.UrlValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafeUrlValidator implements ConstraintValidator<SafeUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return UrlValidationUtil.isSafeHttpUrl(value);
    }
}
