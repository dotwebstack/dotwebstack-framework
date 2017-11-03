package org.dotwebstack.framework.frontend.ld.entity;

import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.QueryResult;

public interface Entity<R extends QueryResult<?>> {

  R getQueryResult();

  Representation getRepresentation();

}
