package org.dotwebstack.framework.backend.rdf4j.query.context;

import static graphql.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.junit.jupiter.api.Test;


class EdgeHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Test
  void testHasEqualTargetClass_returnsTrue_withNullNodeShape() {
    Iri iri = () -> "<http://www.example.com#testType>";

    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> "<" + RDF.TYPE.stringValue() + ">")
                .object(Vertice.builder()
                    .iris(Set.of(iri))
                    .build())
                .build()))
            .build())
        .build();
    PropertyShape shape = PropertyShape.builder()
        .node(NodeShape.builder()
            .targetClasses(Set.of(VF.createIRI("http://www.example.com#testType")))
            .build())
        .build();

    assertTrue(EdgeHelper.hasEqualTargetClass(edge, shape));
  }

}
