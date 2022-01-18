package org.dotwebstack.framework.ext.orchestrate.config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class RootSubschemaExistsValidator
    implements ConstraintValidator<RootSubschemaExists, OrchestrateConfigurationProperties> {

  @Override
  public boolean isValid(OrchestrateConfigurationProperties value, ConstraintValidatorContext context) {
    if (value.getRoot() == null) {
      return true;
    }

    return value.getSubschemas()
        .containsKey(value.getRoot());
  }
}
