package io.reading_tracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class AtLeastOneNotNullValidator implements ConstraintValidator<AtLeastOneNotNull, Object> {

  private String[] fields;

  @Override
  public void initialize(AtLeastOneNotNull constraintAnnotation) {
    this.fields = constraintAnnotation.fields();
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
    if (object == null) {
      return true;
    }

    BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);

    for (String field : fields) {
      Object propertyValue = beanWrapper.getPropertyValue(field);

      if (propertyValue != null) {
        return true;
      }
    }

    return false;
  }
}
