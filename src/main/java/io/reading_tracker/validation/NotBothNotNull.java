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
  String message() default "현재 페이지와 상태 중 하나만 수정할 수 있습니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] fields();
}
