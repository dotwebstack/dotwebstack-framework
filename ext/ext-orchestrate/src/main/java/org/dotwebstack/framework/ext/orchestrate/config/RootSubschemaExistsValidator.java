package org.dotwebstack.framework.ext.orchestrate.config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class RootSubschemaExistsValidator
    implements ConstraintValidator<RootSubschemaExists, OrchestrateConfigurationProperties> {

  @Override
  public boolean isValid(OrchestrateConfigurationProperties value, ConstraintValidatorContext context) {
    return value.getSubschemas()
        .containsKey(value.getRoot());
  }
}
