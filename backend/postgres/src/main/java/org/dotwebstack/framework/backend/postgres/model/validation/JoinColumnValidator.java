package org.dotwebstack.framework.backend.postgres.model.validation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;

public class JoinColumnValidator implements ConstraintValidator<ValidJoinColumn, JoinColumn> {

  @Override
  public boolean isValid(JoinColumn joincolumn, ConstraintValidatorContext context) {
    return isNotBlank(joincolumn.getReferencedField()) || isNotBlank(joincolumn.getReferencedColumn());
  }
}
