package io.reading_tracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotBothNotNullValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBothNotNull {
  String message() default "Only one of the fields may be provided.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] fields();
}
