package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;

@RequiredArgsConstructor
abstract class AbstractQueryBuilder<Q extends OuterQuery<?>> {

  protected final QueryEnvironment environment;

  protected final Q query;
}
