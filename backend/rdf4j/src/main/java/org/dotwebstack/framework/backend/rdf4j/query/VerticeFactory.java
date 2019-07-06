package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionContext;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

class VerticeFactory {

  private VerticeFactory() {}

  static Vertice createVertice(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> fields) {
    Variable root = query.var();
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

    // type edge
    edges.add(createSimpleEdge(null, Rdf.iri(nodeShape.getTargetClass()
        .stringValue()), () -> "<" + RDF.TYPE + ">", false, true));

    // filter edges
    addFilters(query, nodeShape, fields, edges);

    return Vertice.builder()
        .subject(root)
        .edges(edges)
        .build();
  }

  private static Edge createComplexEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field) {
    return Edge.builder()
        .predicate(nodeShape.getPropertyShape(field.getName())
            .getPath()
            .toPredicate())
        .object(createVertice(query, nodeShape.getPropertyShape(field.getName())
            .getNode(),
            field.getSelectionSet()
                .getFields()))
        .isOptional(true)
        .isVisible(true)
        .build();
  }

  private static Edge createSimpleEdge(Variable subject, Iri iri, RdfPredicate predicate, boolean isOptional,
      boolean isVisible) {
    return Edge.builder()
        .predicate(predicate)
        .object(Vertice.builder()
            .subject(subject)
            .iri(iri)
            .build())
        .isVisible(isVisible)
        .isOptional(isOptional)
        .build();
  }

  private static void addFilters(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> fields,
      List<Edge> edges) {
    fields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .filter(field -> !Objects.isNull(nodeShape.getPropertyShape(field.getName())
            .getNode()))
        .forEach(field -> field.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> GraphQLTypeUtil.isList(field.getFieldDefinition()
                .getType()) && argument.getDirective(CoreDirectives.FILTER_NAME) != null)
            .forEach(argument -> edges.stream()
                .filter(edge -> edge.getPredicate()
                    .getQueryString()
                    .equals(nodeShape.getPropertyShape(field.getName())
                        .getPath()
                        .toPredicate()
                        .getQueryString()))
                .filter(edge -> hasChildEdgeOfType(edge, nodeShape.getPropertyShape(field.getName())
                    .getNode()))
                .forEach(edge -> {
                  Object filterValue = field.getArguments()
                      .get(argument.getName());
                  if (!Objects.isNull(filterValue)) {
                    NodeShape childShape = nodeShape.getPropertyShape(field.getName())
                        .getNode();
                    String argumentName = getArgumentName(argument);
                    PropertyShape propertyShape = childShape.getPropertyShape(argumentName);

                    Vertice vertice = edge.getObject();
                    List<Edge> verticeEdges = vertice.getEdges();

                    // add missing edges
                    Edge match = findOrCreateEdge(query, propertyShape, vertice);

                    // add filters to vertice
                    List<ExpressionContext> filters =
                        getExpressionContexts(nodeShape, field, argument, filterValue, argumentName, match);

                    match.getObject()
                        .setFilters(filters);
                    match.setOptional(false);
                    verticeEdges.remove(match);
                    verticeEdges.add(match);
                  }
                })));
  }

  private static String getArgumentName(GraphQLArgument argument) {
    return !Objects.isNull(argument.getDirective(CoreDirectives.FILTER_NAME)
                          .getArgument(CoreDirectives.FILTER_ARG_FIELD)
                          .getValue()) ? (String) argument.getDirective(CoreDirectives.FILTER_NAME)
                              .getArgument(CoreDirectives.FILTER_ARG_FIELD)
                              .getValue() : argument.getName();
  }

  private static boolean hasChildEdgeOfType(Edge edge, NodeShape nodeShape) {
    List<Edge> childEdges = edge.getObject()
        .getEdges();
    return childEdges.stream()
        .anyMatch(childEdge -> ("<" + RDF.TYPE.stringValue() + ">").equals(childEdge.getPredicate()
            .getQueryString())
            && ("<" + nodeShape.getTargetClass()
                .stringValue() + ">").equals(childEdge.getObject()
                    .getIri()
                    .getQueryString()));
  }

  private static List<ExpressionContext> getExpressionContexts(NodeShape nodeShape, SelectedField field,
      GraphQLArgument argument, Object filterValue, String argumentName, Edge match) {
    List<ExpressionContext> filters = Objects.isNull(match.getObject()
        .getFilters()) ? new ArrayList<>()
            : match.getObject()
                .getFilters();

    List<Object> filterArguments;
    if (filterValue instanceof List) {
      filterArguments = ObjectHelper.castToList(filterValue);
    } else {
      filterArguments = Collections.singletonList(filterValue);
    }

    List<Operand> operands = filterArguments.stream()
        .map(filterArgument -> ExpressionHelper.getOperand(nodeShape.getPropertyShape(field.getName())
            .getNode(), argumentName, filterArgument))
        .collect(Collectors.toList());

    filters.add(ExpressionContext.builder()
        .operator(FilterOperator.getByValue((String) argument.getDirective(CoreDirectives.FILTER_NAME)
            .getArgument(CoreDirectives.FILTER_ARG_OPERATOR)
            .getValue())
            .orElse(FilterOperator.EQ))
        .operands(operands)
        .build());
    return filters;
  }

  private static Edge findOrCreateEdge(OuterQuery<?> query, PropertyShape propertyShape, Vertice vertice) {
    List<Edge> childEdges = vertice.getEdges();
    Optional<Edge> optional = childEdges.stream()
        .filter(childEdge -> childEdge.getPredicate()
            .getQueryString()
            .equals(propertyShape.getPath()
                .toPredicate()
                .getQueryString()))
        .findFirst();

    return optional.orElseGet(() -> createSimpleEdge(query.var(), null, propertyShape.getPath()
        .toPredicate(), false, false));
  }
}
