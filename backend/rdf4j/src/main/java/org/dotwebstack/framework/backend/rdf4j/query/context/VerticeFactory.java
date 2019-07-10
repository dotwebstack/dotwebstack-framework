package org.dotwebstack.framework.backend.rdf4j.query.context;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.dotwebstack.framework.core.traversers.DirectiveContainerTuple;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

public class VerticeFactory {

  private VerticeFactory() {}

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Vertice createSubjectVertice(Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<DirectiveContainerTuple> filterMapping, Optional<List> orderByOptional) {
    Vertice vertice = createVertice(subject, query, nodeShape, Collections.emptyList());

    filterMapping.forEach(filter -> {
      GraphQLDirectiveContainer container = filter.getContainer();
      String fieldName = VerticeFactoryHelper.getFieldName(container);
      String[] fieldPath = fieldName.split("\\.");
      String[] startPath = VerticeFactoryHelper.getFieldName(container)
          .split("\\.");
      NodeShape childShape = VerticeFactoryHelper.getNextNodeShape(nodeShape, fieldPath);

      if (nodeShape.equals(childShape)) {
        addFilterToVertice(vertice, container, query, nodeShape, filter.getValue(), fieldPath, startPath);
      } else {
        Edge edge = createSimpleEdge(query.var(), null, nodeShape.getPropertyShape(fieldPath[0])
            .getPath()
            .toPredicate(), false, false);
        fieldPath = ArrayUtils.remove(fieldPath, 0);
        addFilterToVertice(edge.getObject(), container, query, childShape, filter.getValue(), fieldPath, fieldPath);
        vertice.getEdges()
            .add(edge);
      }
    });

    makeEdgesUnique(vertice.getEdges());

    orderByOptional.ifPresent(orderBy -> orderBy
        .forEach(entity -> addOrderContexts(vertice, query, (Map<String, String>) entity, nodeShape)));

    return vertice;
  }

  public static Vertice createVertice(final Variable subject, OuterQuery<?> query, NodeShape nodeShape,
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
        .stringValue()), () -> "<" + RDF.TYPE + ">", false, true));

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
  private static Edge createComplexEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field) {
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

  /*
   * A simple edge is an edge without any vertices or filters added to it
   */
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

  private static HashMap<GraphQLArgument, SelectedField> getArgumentFieldMapping(NodeShape nodeShape,
      List<SelectedField> fields) {
    return fields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .filter(field -> Objects.nonNull(nodeShape.getPropertyShape(field.getName())
            .getNode()))
        .flatMap(field -> field.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> argument.getDirective(CoreDirectives.FILTER_NAME) != null)
            .map(argument -> new AbstractMap.SimpleEntry<>(argument, field)))
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  private static List<Edge> findEdgesToBeProcessed(NodeShape nodeShape, SelectedField field, List<Edge> edges) {
    return edges.stream()
        .filter(edge -> edge.getPredicate()
            .getQueryString()
            .equals(nodeShape.getPropertyShape(field.getName())
                .getPath()
                .toPredicate()
                .getQueryString()))
        .filter(edge -> {
          if (!GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(field.getFieldDefinition()
              .getType()))) {
            return VerticeFactoryHelper.hasChildEdgeOfType(edge, nodeShape.getPropertyShape(field.getName())
                .getNode()
                .getTargetClass());
          }
          return true;
        })
        .collect(Collectors.toList());
  }

  private static void processEdge(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, NodeShape nodeShape,
      SelectedField field) {
    Object filterValue = field.getArguments()
        .get(argument.getName());
    if (Objects.nonNull(filterValue)) {
      String[] startPath = VerticeFactoryHelper.getFieldName(argument)
          .split("\\.");
      String[] fieldPath = field.getName()
          .split("\\.");

      addFilterToVertice(vertice, argument, query, VerticeFactoryHelper.getNextNodeShape(nodeShape, fieldPath),
          filterValue, startPath, startPath);
    }
  }

  private static void addFilterToVertice(Vertice vertice, GraphQLDirectiveContainer container, OuterQuery<?> query,
      NodeShape nodeShape, Object filterValue, String[] fieldPath, String[] startPath) {
    Edge match = findOrCreatePath(vertice, query, nodeShape, startPath);

    List<Filter> filters = Objects.nonNull(match.getObject()
        .getFilters()) ? match.getObject()
            .getFilters() : new ArrayList<>();
    Filter filter = createFilter(nodeShape, container, filterValue, fieldPath[fieldPath.length - 1]);
    filters.add(filter);

    match.getObject()
        .setFilters(filters);
    match.setOptional(false);
  }

  /*
   * Create a new filter with either one argument or a list of arguments
   */
  private static Filter createFilter(NodeShape nodeShape, GraphQLDirectiveContainer container, Object filterValue,
      String argumentName) {
    List<Object> filterArguments;
    if (filterValue instanceof List) {
      filterArguments = ObjectHelper.castToList(filterValue);
    } else {
      filterArguments = Collections.singletonList(filterValue);
    }

    List<Operand> operands = filterArguments.stream()
        .map(filterArgument -> FilterHelper.getOperand(nodeShape, argumentName, filterArgument))
        .collect(Collectors.toList());

    return Filter.builder()
        .operator(FilterOperator.getByValue((String) container.getDirective(CoreDirectives.FILTER_NAME)
            .getArgument(CoreDirectives.FILTER_ARG_OPERATOR)
            .getValue())
            .orElse(FilterOperator.EQ))
        .operands(operands)
        .build();
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private static Edge findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, String[] fieldPaths) {
    Edge match = findOrCreateEdge(query, nodeShape.getPropertyShape(fieldPaths[0]), vertice);

    if (fieldPaths.length == 1) {
      return match;
    }

    NodeShape childShape = VerticeFactoryHelper.getNextNodeShape(nodeShape, fieldPaths);
    return findOrCreatePath(match.getObject(), query, childShape, ArrayUtils.remove(fieldPaths, 0));
  }

  /*
   * Find the edge belonging to the given propertyshape. In case no propertyshape is found, create a
   * new one
   */
  private static Edge findOrCreateEdge(OuterQuery<?> query, PropertyShape propertyShape, Vertice vertice) {
    List<Edge> childEdges = vertice.getEdges();

    Optional<Edge> optional = Optional.empty();
    if (Objects.nonNull(childEdges)) {
      optional = childEdges.stream()
          .filter(childEdge -> childEdge.getPredicate()
              .getQueryString()
              .equals(propertyShape.getPath()
                  .toPredicate()
                  .getQueryString()))
          .findFirst();
    }

    return optional.orElseGet(() -> {
      Edge edge = createSimpleEdge(query.var(), null, propertyShape.getPath()
          .toPredicate(), false, false);
      List<Edge> edges = Objects.nonNull(vertice.getEdges()) ? vertice.getEdges() : new ArrayList<>();
      edges.add(edge);
      vertice.setEdges(edges);
      return edge;
    });
  }

  /*
   * It can happen that the same path is used twice, we want to overcome this problem, by looking at
   * the edges, if we find more edges with the same predicate, we place the child edges of the latter
   * edges we find, on top of the first edge we find.
   */
  private static void makeEdgesUnique(List<Edge> edges) {
    List<Edge> uniqueEdges = new ArrayList<>();
    edges.forEach(edge -> {
      Optional<Edge> optional = uniqueEdges.stream()
          .filter(uniqueEdge -> uniqueEdge.getPredicate()
              .equals(edge.getPredicate()))
          .findFirst();

      if (optional.isPresent()) {
        List<Edge> childEdges = edge.getObject()
            .getEdges();
        optional.get()
            .getObject()
            .getEdges()
            .addAll(childEdges);
      } else {
        uniqueEdges.add(edge);
      }
    });
  }

  private static void addOrderContexts(Vertice vertice, OuterQuery<?> query, Map<String, String> orderMap,
      NodeShape nodeShape) {
    String fieldName = orderMap.get("field");
    String order = orderMap.get("order");

    String[] fieldPaths = fieldName.split("\\.");
    NodeShape childShape = VerticeFactoryHelper.getNextNodeShape(nodeShape, fieldPaths);

    // add missing edges
    Edge match;
    Variable subject;
    if (nodeShape.equals(childShape)) {
      match = findOrCreatePath(vertice, query, nodeShape, fieldPaths);
      subject = VerticeFactoryHelper.getSubjectForField(match, nodeShape, fieldPaths);
    } else {
      match = findOrCreatePath(vertice, query, childShape, ArrayUtils.remove(fieldPaths, 0));
      subject = VerticeFactoryHelper.getSubjectForField(match, childShape, ArrayUtils.remove(fieldPaths, 0));
    }

    List<Orderable> orderables = Objects.nonNull(vertice.getOrderables()) ? vertice.getOrderables() : new ArrayList<>();
    orderables.add(order.equalsIgnoreCase("desc") ? subject.desc() : subject.asc());
    vertice.setOrderables(orderables);
  }
}
