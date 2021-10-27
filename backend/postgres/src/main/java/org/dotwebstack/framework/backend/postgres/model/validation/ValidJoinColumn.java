package org.dotwebstack.framework.backend.postgres.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = JoinColumnValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidJoinColumn {

  String message() default "referencedField or referencedColumn must have a value.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
