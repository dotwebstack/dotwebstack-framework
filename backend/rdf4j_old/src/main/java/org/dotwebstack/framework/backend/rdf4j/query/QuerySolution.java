package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

@Getter
public final class QuerySolution {

  private final Model model;

  private final Resource subject;

  public QuerySolution(Model model, Resource subject) {
    this.model = model;
    this.subject = subject;
  }
}
