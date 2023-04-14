package org.dotwebstack.framework.backend.postgres.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = JoinColumnValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidJoinColumn {

  String message() default "referencedField or referencedColumn must have a value.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
