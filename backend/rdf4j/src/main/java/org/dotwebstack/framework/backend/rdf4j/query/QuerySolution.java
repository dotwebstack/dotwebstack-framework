package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

@Getter
@RequiredArgsConstructor
final class QuerySolution {

  private final Model model;

  private final Resource subject;

}
