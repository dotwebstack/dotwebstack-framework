package org.dotwebstack.framework.backend.rdf4j.shacl;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

@Getter
public enum ConstraintType {
  MINCOUNT(SHACL.MIN_COUNT), MAXCOUNT(SHACL.MAX_COUNT), HASVALUE(SHACL.HAS_VALUE);

  private final IRI shaclType;

  ConstraintType(IRI shaclType) {
    this.shaclType = shaclType;
  }
}
