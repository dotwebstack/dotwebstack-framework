package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.isOfType;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.stringify;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

public class ConstructVerticeFactory extends AbstractVerticeFactory {

  public Vertice createVertice(List<IRI> filterSubjects, final Variable subject, OuterQuery<?> query,
      NodeShape nodeShape, List<SelectedField> fields) {
    Vertice vertice = createVertice(subject, query, nodeShape, fields);
    vertice.getEdges()
        .stream()
        .filter(childEdge -> isOfType(childEdge, nodeShape.getTargetClass()))
        .findFirst()
        .ifPresent(edge -> addSubjectFilters(edge.getObject(), filterSubjects));

    return vertice;
  }

  private Vertice createVertice(final Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<SelectedField> fields) {
    List<Edge> edges = fields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .map(field -> {
          PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
          NodeShape childShape = propertyShape.getNode();

          if (Objects.isNull(childShape)) {
            return createSimpleEdge(query.var(), null, propertyShape.getPath()
                .toPredicate(), true, true);
          }
          return createComplexEdge(query, nodeShape, field);
        })
        .collect(Collectors.toList());

    makeEdgesUnique(edges);

    edges.add(createSimpleEdge(null, Rdf.iri(nodeShape.getTargetClass()
        .stringValue()), () -> stringify(RDF.TYPE), false, true));

    getArgumentFieldMapping(nodeShape, fields)
        .forEach((argument, field) -> findEdgesToBeProcessed(nodeShape, field, edges)
            .forEach(edge -> processEdge(edge.getObject(), argument, query, nodeShape, field)));

    return Vertice.builder()
        .subject(subject)
        .edges(edges)
        .build();
  }

  /*
   * A complex edge is an edge with filters vertices/filters added to it
   */
  private Edge createComplexEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field) {
    return Edge.builder()
        .predicate(nodeShape.getPropertyShape(field.getName())
            .getPath()
            .toPredicate())
        .object(createVertice(query.var(), query, nodeShape.getPropertyShape(field.getName())
            .getNode(),
            field.getSelectionSet()
                .getFields()))
        .isOptional(true)
        .isVisible(true)
        .build();
  }

  private void addSubjectFilters(Vertice vertice, List<IRI> filterSubjects) {
    vertice.getFilters()
        .add(Filter.builder()
            .operator(FilterOperator.EQ)
            .operands(filterSubjects.stream()
                .map(Rdf::iri)
                .collect(Collectors.toList()))
            .build());
  }
}
