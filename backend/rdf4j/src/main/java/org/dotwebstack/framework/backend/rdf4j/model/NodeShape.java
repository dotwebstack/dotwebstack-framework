package org.dotwebstack.framework.backend.rdf4j.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

@Builder
@Getter
public final class NodeShape {

  private final Resource identifier;

  private final IRI targetClass;

  private final Map<String, PropertyShape> propertyShapes;

}
