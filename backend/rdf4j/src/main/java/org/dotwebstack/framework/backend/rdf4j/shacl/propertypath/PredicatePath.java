package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import lombok.Builder;
import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;

@Builder
@Getter
public class PredicatePath implements PropertyPath {

  private final IRI iri;

}
