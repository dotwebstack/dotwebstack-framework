package org.dotwebstack.framework.backend.rdf4j.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Rdf4jKeyCriteria extends KeyCriteria {

  private final RdfPredicate predicate;
}
