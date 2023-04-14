package org.dotwebstack.framework.backend.postgres.model.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.junit.jupiter.api.Test;

class JoinColumnValidatorTest {

  private final JoinColumnValidator validator = new JoinColumnValidator();

  @Test
  void isValid_returnsTrue() {
    JoinColumn joincolumn = new JoinColumn();
    joincolumn.setName("a");
    joincolumn.setReferencedField("bb");
    joincolumn.setReferencedColumn("ccc");
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertTrue(validator.isValid(joincolumn, context));
  }

  @Test
  void isValid_returnsTrueFalse() {
    JoinColumn joincolumn = new JoinColumn();
    joincolumn.setName("a");
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertFalse(validator.isValid(joincolumn, context));
  }
}
