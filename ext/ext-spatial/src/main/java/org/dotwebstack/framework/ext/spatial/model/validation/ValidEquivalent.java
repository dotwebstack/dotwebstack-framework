package org.dotwebstack.framework.ext.spatial.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = EquivalentValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEquivalent {

  String message() default "(srid) has one or more srs values with an equivalent property which "
      + "is not three dimensional or points to a two dimensional srs.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
