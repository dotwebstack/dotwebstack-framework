package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getFilterRulePath;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getOperand;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getSubjectForField;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasChildEdgeOfType;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions.custom;

import com.google.common.collect.ImmutableList;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.input.CoreInputTypes;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

abstract class AbstractVerticeFactory {

  private SerializerRouter serializerRouter;

  private Rdf4jProperties rdf4jProperties;

  public AbstractVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    this.serializerRouter = serializerRouter;
    this.rdf4jProperties = rdf4jProperties;
  }

  Edge createSimpleEdge(Variable subject, BasePath basePath, boolean isOptional, boolean isVisible) {
    return Edge.builder()
        .predicate(basePath.toPredicate())
        .constructPredicate(basePath.toConstructPredicate())
        .object(Vertice.builder()
            .subject(subject)
            .build())
        .isVisible(isVisible)
        .isOptional(isOptional)
        .build();
  }

  Edge createSimpleEdge(Variable subject, Set<Iri> iris, RdfPredicate predicate, boolean isVisible) {
    return Edge.builder()
        .predicate(predicate)
        .object(Vertice.builder()
            .subject(subject)
            .iris(iris)
            .build())
        .isVisible(isVisible)
        .isOptional(false)
        .build();
  }

  Map<GraphQLArgument, SelectedField> getArgumentFieldMapping(NodeShape nodeShape, List<SelectedField> fields,
      String directiveName) {
    return fields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .filter(field -> nonNull(nodeShape.getPropertyShape(field.getName())
            .getNode()))
        .flatMap(field -> field.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> argument.getDirective(directiveName) != null)
            .map(argument -> new AbstractMap.SimpleEntry<>(argument, field)))
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  List<Edge> findEdgesToBeProcessed(NodeShape nodeShape, SelectedField field, List<Edge> edges) {
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
            return hasChildEdgeOfType(edge, nodeShape.getPropertyShape(field.getName())
                .getNode()
                .getTargetClasses());
          }
          return true;
        })
        .collect(Collectors.toList());
  }

  @SuppressWarnings({"unchecked"})
  void processEdgeSort(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, NodeShape nodeShape,
      SelectedField field) {
    Object orderByList = nonNull(argument.getValue()) ? argument.getValue() : argument.getDefaultValue();
    Map<String, Object> orderMap = castToMap(((List<Object>) orderByList).get(0));

    String fieldName = orderMap.getOrDefault("field", argument.getName())
        .toString();

    if (nonNull(fieldName)) {
      findOrCreatePath(vertice, query, nodeShape.getPropertyShape(field.getName())
          .getNode(), new ArrayList<>(Arrays.asList(fieldName.split("\\."))), false, true, singletonList(field));
    }
  }

  void processEdge(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, NodeShape nodeShape,
      SelectedField field) {
    Object filterValue = field.getArguments()
        .get(argument.getName());
    if (nonNull(filterValue)) {
      List<String> startPath = getFilterRulePath(argument);
      String[] fieldPath = field.getName()
          .split("\\.");

      addFilterToVertice(vertice, query, getNextNodeShape(nodeShape, Arrays.asList(fieldPath)), FilterRule.builder()
          .path(startPath)
          .value(filterValue)
          .build(), singletonList(field));
    }
  }

  void addFilterToVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule,
      List<SelectedField> fields) {
    findOrCreatePath(vertice, query, nodeShape, filterRule.getPath(), true, false, fields)
        .ifPresent(match -> addFilterToVertice(nodeShape, match, filterRule));
  }

  void addFilterToVertice(NodeShape nodeShape, Edge edge, FilterRule filterRule) {
    addFilterToVertice(nodeShape, edge.getObject(), filterRule, null);
  }

  void addFilterToVertice(NodeShape nodeShape, Vertice vertice, FilterRule filterRule, Edge referredEdge) {
    List<Filter> filters = nonNull(vertice.getFilters()) ? vertice.getFilters() : new ArrayList<>();

    Filter filter = createFilter(nodeShape, filterRule.getOperator(), filterRule.getValue(), filterRule.getPath()
        .get(filterRule.getPath()
            .size() - 1));

    filter.setEdge(referredEdge);

    filters.add(filter);

    vertice.setFilters(filters);
  }


  /*
   * Create a new filter with either one argument or a list of arguments
   */
  private Filter createFilter(NodeShape nodeShape, String filterOperator, Object filterValue, String argumentName) {
    List<Object> filterArguments;
    if (filterValue instanceof List) {
      filterArguments = castToList(filterValue);
    } else {
      filterArguments = singletonList(filterValue);
    }

    List<Operand> operands = filterArguments.stream()
        .map(filterArgument -> getOperand(nodeShape, argumentName, serializerRouter.serialize(filterArgument),
            rdf4jProperties.getShape()
                .getLanguage()))
        .collect(Collectors.toList());

    return Filter.builder()
        .operator(FilterOperator.getByValue(filterOperator)
            .orElse(FilterOperator.EQ))
        .operands(operands)
        .build();
  }

  private void getEdgeFromField(OuterQuery<?> query, Edge edge, SelectedField selectedField) {
    String aggregateType = DirectiveUtils.getArgument(selectedField.getFieldDefinition(),
        Rdf4jDirectives.AGGREGATE_NAME, CoreInputTypes.AGGREGATE_TYPE, String.class);
    edge.setAggregate(Aggregate.builder()
        .type(aggregateType)
        .variable(query.var())
        .build());
  }

  /*
   * Find the edge belonging to the given propertyshape. In case no propertyshape is found, create a
   * new one
   */
  private Edge findOrCreateEdge(OuterQuery<?> query, PropertyShape propertyShape, Vertice vertice, boolean required,
      boolean isVisible) {
    List<Edge> childEdges = vertice.getEdges();

    Optional<Edge> optional = Optional.empty();
    if (nonNull(childEdges)) {
      optional = childEdges.stream()
          .filter(childEdge -> childEdge.getPredicate()
              .getQueryString()
              .equals(propertyShape.getPath()
                  .toPredicate()
                  .getQueryString()))
          .filter(childEdge -> isNull(propertyShape.getNode()) || hasChildEdgeOfType(childEdge, propertyShape.getNode()
              .getTargetClasses()))
          .findFirst();
    }

    return optional.orElseGet(() -> {
      Edge edge = createSimpleEdge(query.var(), propertyShape.getPath(), !required, isVisible);
      vertice.getEdges()
          .add(edge);
      return edge;
    });
  }

  /*
   * It can happen that the same path is used twice, we want to overcome this problem, by looking at
   * the edges, if we find more edges with the same predicate, we place the child edges of the latter
   * edges we find, on top of the first edge we find.
   */
  void makeEdgesUnique(List<Edge> edges) {
    List<Edge> uniqueEdges = new ArrayList<>();
    edges.forEach(edge -> {
      Edge duplicate = uniqueEdges.stream()
          .filter(uniqueEdge -> uniqueEdge.getPredicate()
              .equals(edge.getPredicate()))
          .findFirst()
          .orElse(null);

      if (nonNull(duplicate)) {
        List<Edge> childEdges = edge.getObject()
            .getEdges();
        duplicate.getObject()
            .getEdges()
            .addAll(childEdges);
      } else {
        uniqueEdges.add(edge);
      }
    });
  }

  void addOrderables(Vertice vertice, OuterQuery<?> query, Map<String, Object> orderMap, NodeShape nodeShape,
      List<SelectedField> fields) {
    String fieldName = orderMap.get("field")
        .toString();
    String order = orderMap.get("order")
        .toString();

    final List<String> fieldPaths = Arrays.asList(fieldName.split("\\."));

    // add missing edges
    Optional<Variable> subject = findOrCreatePath(vertice, query, nodeShape, fields, fieldPaths);

    subject.ifPresent(s -> {
      List<Orderable> orderables = nonNull(vertice.getOrderables()) ? vertice.getOrderables() : new ArrayList<>();
      orderables.add(custom(() -> "", Expressions.not(Expressions.bound(s))));

      orderables.add((isNull(order) || order.equalsIgnoreCase("desc")) ? s.desc() : s.asc());
      vertice.setOrderables(orderables);
    });
  }

  private Optional<Variable> findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<SelectedField> fields, List<String> fieldPaths) {
    NodeShape childShape = getNextNodeShape(nodeShape, fieldPaths);
    if (nodeShape.equals(childShape) || fieldPaths.size() == 1) {
      return findOrCreatePath(vertice, query, nodeShape, fieldPaths, false, false, fields)
          .map(edge -> getSubjectForField(edge, nodeShape, fieldPaths));

    } else {
      Edge edge = createSimpleEdge(query.var(), nodeShape.getPropertyShape(fieldPaths.get(0))
          .getPath(), true, false);
      vertice.getEdges()
          .add(edge);
      final List<String> subFieldPaths = fieldPaths.subList(1, fieldPaths.size());
      return findOrCreatePath(edge.getObject(), query, childShape, subFieldPaths, false, false, fields)
          .map(e -> getSubjectForField(e, childShape, subFieldPaths));
    }
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Optional<Edge> findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<String> fieldPaths, boolean required, boolean isVisible, List<SelectedField> fields) {

    if (fieldPaths.size() == 0) {
      return Optional.empty();
    }

    PropertyShape propertyShape = nodeShape.getPropertyShape(fieldPaths.get(0));
    Edge match = findOrCreateEdge(query, propertyShape, vertice, required, isVisible);
    if (required) {
      match.setOptional(false);
    }

    NodeShape childShape = getNextNodeShape(nodeShape, fieldPaths);

    if (fieldPaths.size() == 1) {
      Optional<SelectedField> fieldWithAggregate = fields.stream()
          .filter(field -> Objects.equals(field.getName(), fieldPaths.get(0)) && nonNull(field.getFieldDefinition()
              .getDirective(Rdf4jDirectives.AGGREGATE_NAME)))
          .findFirst();
      fieldWithAggregate.ifPresent(field -> getEdgeFromField(query, match, field));
      return Optional.of(match);
    }

    return findOrCreatePath(match.getObject(), query, childShape, fieldPaths.subList(1, fieldPaths.size()), required,
        isVisible, fields);
  }

  void addLanguageFilter(Edge edge, PropertyShape propertyShape) {
    if (Objects.equals(RDF.LANGSTRING, propertyShape.getDatatype())) {
      edge.getObject()
          .getFilters()
          .add(createLanguageFilter());
    }
  }

  private Filter createLanguageFilter() {
    return Filter.builder()
        .operator(FilterOperator.LANGUAGE)
        .operands(ImmutableList.of(Rdf.literalOf(rdf4jProperties.getShape()
            .getLanguage())))
        .build();
  }
}
