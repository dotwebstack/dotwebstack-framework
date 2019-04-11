package org.dotwebstack.framework.backend.rdf4j.graphql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

@Getter
@RequiredArgsConstructor
final class QuerySolution {

  private final Model model;

  private final IRI subject;

}
