package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

@Data
@Builder
public class Vertice {

  private NodeShape nodeShape;

  private Variable subject;

  @Builder.Default
  private Set<Iri> iris = new HashSet<>();

  private Value value;

  @Builder.Default
  private List<Edge> edges = new ArrayList<>();

  @Builder.Default
  private List<Filter> filters = new ArrayList<>();

  @Builder.Default
  private List<Orderable> orderables = new ArrayList<>();
}
