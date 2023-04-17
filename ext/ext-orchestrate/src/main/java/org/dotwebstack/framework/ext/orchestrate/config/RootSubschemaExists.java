package org.dotwebstack.framework.ext.orchestrate.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RootSubschemaExistsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface RootSubschemaExists {

  String message() default "'dotwebstack.orchestrate.root' must refer to an existing subschema";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
