package org.dotwebstack.framework.backend.rdf4j.query;

import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;

abstract class AbstractQueryBuilder<Q extends OuterQuery<?>> {

  protected final QueryEnvironment environment;

  protected final Q query;

  public AbstractQueryBuilder(QueryEnvironment environment, Q query) {
    this.environment = environment;
    this.query = query;
  }
}
