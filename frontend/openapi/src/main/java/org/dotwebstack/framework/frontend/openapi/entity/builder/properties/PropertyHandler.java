package org.dotwebstack.framework.frontend.openapi.entity.builder.properties;

import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.eclipse.rdf4j.model.Value;

public interface PropertyHandler<P extends Property> {

  Object handle(P property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context);

  boolean supports(Property property);

}
