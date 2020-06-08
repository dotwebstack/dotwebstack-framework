package org.dotwebstack.framework.backend.rdf4j.shacl;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

@Getter
public enum ConstraintType {
  MINCOUNT(SHACL.MIN_COUNT), MAXCOUNT(SHACL.MAX_COUNT), RDF_TYPE(RDF.TYPE), HASVALUE(SHACL.HAS_VALUE);

  private final IRI type;

  ConstraintType(IRI type) {
    this.type = type;
  }
}
