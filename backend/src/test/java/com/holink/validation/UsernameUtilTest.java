package com.holink.validation;

import com.holink.validation.util.UsernameUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsernameUtilTest {

    @Test
    void normalize_mixedCaseWithSpaces() {
        assertEquals("johndoe", UsernameUtil.normalize("John Doe"));
    }

    @Test
    void validateRawInput_rejectsAllUppercase() {
        assertThrows(IllegalArgumentException.class,
                () -> UsernameUtil.validateRawInput("UPPERCASE"));
    }

    @Test
    void validateRawInput_rejectsLowercaseWithSpaces() {
        assertThrows(IllegalArgumentException.class,
                () -> UsernameUtil.validateRawInput("john doe"));
    }

    @Test
    void validateRawInput_allowsMixedCaseWithSpaces() {
        UsernameUtil.validateRawInput("John Doe");
    }

    @Test
    void validateRawInput_rejectsAtSymbol() {
        assertThrows(IllegalArgumentException.class,
                () -> UsernameUtil.validateRawInput("@john"));
    }
}
