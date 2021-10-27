package org.dotwebstack.framework.core.testhelpers;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class TestJoinColumnValidator implements ConstraintValidator<TestValidJoinColumn, TestJoinColumn> {

  @Override
  public boolean isValid(TestJoinColumn joincolumn, ConstraintValidatorContext context) {
    return isNotBlank(joincolumn.getReferencedField()) || isNotBlank(joincolumn.getReferencedColumn());
  }
}
