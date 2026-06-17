package com.holink.validation;

import com.holink.validation.util.SanitizationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoHtmlContentValidator implements ConstraintValidator<NoHtmlContent, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return SanitizationUtil.isFreeOfHtml(value);
    }
}
