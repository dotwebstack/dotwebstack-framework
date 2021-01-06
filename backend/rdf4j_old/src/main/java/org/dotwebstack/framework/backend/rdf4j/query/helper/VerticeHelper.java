package org.dotwebstack.framework.backend.rdf4j.query.helper;

import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class VerticeHelper {

  private VerticeHelper() {}

  public static Vertice buildVertice(Variable subject, NodeShape nodeShape) {
    return Vertice.builder()
        .nodeShape(nodeShape)
        .subject(subject)
        .build();
  }

}
