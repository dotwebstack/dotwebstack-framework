package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Getter
@RequiredArgsConstructor
class JoinCondition {

  private final Resource resource;

  private final RdfPredicate predicate;
}
