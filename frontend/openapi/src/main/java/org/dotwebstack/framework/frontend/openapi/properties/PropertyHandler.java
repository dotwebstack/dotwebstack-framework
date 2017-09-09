package org.dotwebstack.framework.frontend.openapi.properties;

import io.swagger.models.properties.Property;
import org.eclipse.rdf4j.model.Value;

public interface PropertyHandler<P extends Property, R> {

  R handle(P property, Value value);

  boolean supports(Property property);

}
