package org.dotwebstack.framework.backend.rdf4j.shacl;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;


@Builder
@Getter
public final class PropertyShape {

  private final Resource identifier;

  private final String name;

  private final BasePath path;

  private final Integer minCount;

  private final Integer maxCount;

  private final IRI nodeKind;

  private final NodeShape node;

  private final IRI datatype;

}
