package org.dotwebstack.framework.core.testhelpers;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TestJoinColumnValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface TestValidJoinColumn {

  String message() default "referencedField or referencedColumn must have a value.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
