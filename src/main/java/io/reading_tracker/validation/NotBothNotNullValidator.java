package io.reading_tracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class NotBothNotNullValidator implements ConstraintValidator<NotBothNotNull, Object> {

  private String[] fields;

  @Override
  public void initialize(NotBothNotNull constraintAnnotation) {
    this.fields = constraintAnnotation.fields();
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
    if (object == null) {
      return true;
    }

    BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);

    int notNullCount = 0;

    for (String field : fields) {
      Object propertyValue = beanWrapper.getPropertyValue(field);

      if (propertyValue != null) {
        notNullCount++;
      }
    }

    return notNullCount <= 1;
  }
}
