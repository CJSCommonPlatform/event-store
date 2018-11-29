package uk.gov.justice.test.utils;

import java.lang.reflect.Field;

public class FieldAccessor {

    @SuppressWarnings({"unused", "unchecked"})
    public static <T> T getFieldFrom(final Object object, final String fieldName, final Class<T> fieldClass) throws Exception {
        final Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }
}
