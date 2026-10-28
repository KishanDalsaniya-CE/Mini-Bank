package util;

import model.annotation.Id;
import model.annotation.MaxLength;
import model.annotation.Positive;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class AnnotationValidator {
    private AnnotationValidator() {}

    public static List<String> validate(Object object) {
        List<String> errors = new ArrayList<>();
        if (object == null) {
            errors.add("Object cannot be null.");
            return errors;
        }

        Class<?> type = object.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(object);

                    if (field.isAnnotationPresent(Id.class)
                            && (value == null || value.toString().isBlank())) {
                        errors.add(field.getName() + " cannot be empty.");
                    }

                    if (field.isAnnotationPresent(Positive.class)) {
                        Positive annotation = field.getAnnotation(Positive.class);
                        if (value instanceof Number number && number.doubleValue() <= 0) {
                            errors.add(field.getName() + " " + annotation.message());
                        }
                    }

                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength annotation = field.getAnnotation(MaxLength.class);
                        if (value instanceof String text && text.length() > annotation.value()) {
                            errors.add(field.getName() + " cannot exceed "
                                    + annotation.value() + " characters.");
                        }
                    }
                } catch (ReflectiveOperationException | RuntimeException e) {
                    errors.add("Could not validate field: " + field.getName());
                }
            }
            type = type.getSuperclass();
        }
        return errors;
    }
}
