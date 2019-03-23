package org.dotwebstack.framework.backend.rdf4j.model;

import lombok.Builder;
import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;

@Builder
@Getter
public final class ShapeMapping {

  private final IRI shapeUri;

  private final IRI shapeGraph;

}
