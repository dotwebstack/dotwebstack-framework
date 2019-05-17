package org.dotwebstack.framework.backend.rdf4j.shacl;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

@Builder
@Getter
public final class PropertyShape {

  private final Resource identifier;

  private final String name;

  private final PropertyPath path;

  private final int minCount;

  private final int maxCount;

  private final IRI nodeKind;

  private final IRI datatype;

}
