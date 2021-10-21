package org.dotwebstack.framework.ext.orchestrate.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = RootSubschemaExistsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface RootSubschemaExists {

  String message() default "'dotwebstack.orchestrate.root' must refer to an existing subschema";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
