package org.dotwebstack.framework.backend.postgres.model.validation;

import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class JoinColumnValidatorTest {
  
  private JoinColumnValidator validator = new JoinColumnValidator();
  
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
