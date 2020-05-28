package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.Test;

public class VerticeFactoryHelperTest {

  @Test
  public void hasSameType_returnsTrue_forSameEdge() {
    Edge edge1 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.TYPE))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/beer/def#Brewery>"))
                    .build())
                .build()))
            .build())
        .build();

    assertTrue(VerticeFactoryHelper.hasSameType(edge1, edge1));
  }

  @Test
  public void hasSameType_returnsFalse_forEdgesWithDifferentTypes() {
    Edge edge1 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.TYPE))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/beer/def#Brewery>"))
                    .build())
                .build()))
            .build())
        .build();

    Edge edge2 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.TYPE))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/beer/def#Beer>"))
                    .build())
                .build()))
            .build())
        .build();

    assertFalse(VerticeFactoryHelper.hasSameType(edge1, edge2));
  }

  @Test
  public void hasSameType_returnsFalse_forEdgeWithoutAType() {
    Edge edge1 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.LANGSTRING))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/languages/def#Spanish>"))
                    .build())
                .build()))
            .build())
        .build();

    Edge edge2 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.TYPE))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/beer/def#Beer>"))
                    .build())
                .build()))
            .build())
        .build();

    assertFalse(VerticeFactoryHelper.hasSameType(edge1, edge2));
  }

  @Test
  public void hasSameType_returnsTrue_forEdgesWithOverlappingType() {
    Edge edge1 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.TYPE))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/beer/def#Brewery>",
                        () -> "<https://github.com/dotwebstack/beer/def#Beer>"))
                    .build())
                .build()))
            .build())
        .build();

    Edge edge2 = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> stringify(RDF.TYPE))
                .object(Vertice.builder()
                    .iris(Set.of(() -> "<https://github.com/dotwebstack/beer/def#Beer>",
                        () -> "<https://github.com/dotwebstack/beer/def#Ingredient>"))
                    .build())
                .build()))
            .build())
        .build();

    assertTrue(VerticeFactoryHelper.hasSameType(edge1, edge2));
  }

}
