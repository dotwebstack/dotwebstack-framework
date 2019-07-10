package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

@Getter
@Setter
@Builder
public class Vertice {

  private Variable subject;

  private Iri iri;

  private List<Edge> edges;

  private List<Filter> filters;

  private List<Orderable> orderables;
}
