package org.dotwebstack.framework.frontend.ld.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.dotwebstack.framework.backend.ResultType;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SparqlProvider {

  ResultType resultType();

}
