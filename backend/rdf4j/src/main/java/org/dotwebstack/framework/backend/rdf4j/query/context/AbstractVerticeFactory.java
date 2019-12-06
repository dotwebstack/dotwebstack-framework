package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.helper.FieldPathHelper.getFieldDefinitions;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getFilterRulePath;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getOperand;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getSubjectForField;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasChildEdgeOfType;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
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
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;

abstract class AbstractVerticeFactory {

  private static List<GraphQLScalarType> NUMERIC_TYPES = Arrays.asList(Scalars.GraphQLInt, Scalars.GraphQLFloat,
      Scalars.GraphQLBigDecimal, Scalars.GraphQLBigDecimal, Scalars.GraphQLLong, Scalars.GraphQLBigInteger);

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
  void processSort(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, PropertyShape propertyShape,
      List<SelectedField> selectedFields) {
    Object orderByList = nonNull(argument.getValue()) ? argument.getValue() : argument.getDefaultValue();
    Map<String, Object> orderMap = castToMap(((List<Object>) orderByList).get(0));

    String fieldName = orderMap.getOrDefault("field", argument.getName())
        .toString();

    SelectedField selectedField = getSelectedField(selectedFields, fieldName);

    if (nonNull(fieldName)) {
      NodeShape nodeShape = null;
      if (Objects.nonNull(propertyShape.getNode())) {
        nodeShape = propertyShape.getNode();
      }

      findOrCreatePath(vertice, query, nodeShape, getFieldDefinitions(selectedField, fieldName), false, true);
    }
  }

  private SelectedField getSelectedField(List<SelectedField> selectedFields, String fieldName) {
    return selectedFields.stream()
        .filter(selectedField -> Objects.equals(selectedField.getName(), fieldName))
        .findFirst()
        .orElse(null);
  }

  void processFilters(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, PropertyShape propertyShape,
      SelectedField field) {
    Object filterValue = field.getArguments()
        .get(argument.getName());
    if (nonNull(filterValue)) {
      GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(field.getFieldDefinition()
          .getType());

      List<GraphQLFieldDefinition> fieldPath = getFilterRulePath(objectType, argument);

      NodeShape nodeShape = null;
      if (Objects.nonNull(propertyShape.getNode())) {
        nodeShape = propertyShape.getNode();
      }

      addFilterToVertice(vertice, query, nodeShape, FilterRule.builder()
          .path(fieldPath)
          .value(filterValue)
          .build());
    }
  }

  void addFilterToVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    findOrCreatePath(vertice, query, nodeShape, filterRule.getPath(), true, false)
        .ifPresent(match -> addFilterToVertice(nodeShape, match, filterRule));
  }

  void addFilterToVertice(NodeShape nodeShape, Edge edge, FilterRule filterRule) {
    addFilterToVertice(nodeShape, edge.getObject(), filterRule, null);
  }

  void addFilterToVertice(NodeShape nodeShape, Vertice vertice, FilterRule filterRule, Edge referredEdge) {
    List<Filter> filters = nonNull(vertice.getFilters()) ? vertice.getFilters() : new ArrayList<>();

    Filter filter = createFilter(nodeShape, filterRule.getOperator(), filterRule.getValue(), filterRule.getPath()
        .get(filterRule.getPath()
            .size() - 1)
        .getName());

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
      GraphQLObjectType objectType) {
    String fieldName = orderMap.get("field")
        .toString();
    String order = orderMap.get("order")
        .toString();


    final List<GraphQLFieldDefinition> fieldPaths = getFieldDefinitions(objectType, fieldName);

    // add missing edges
    Optional<Variable> subject = findOrCreatePath(vertice, query, nodeShape, fieldPaths);

    subject.map(s -> Expressions.coalesce(s, getDefaultOrderByValue(fieldPaths.get(fieldPaths.size() - 1))))
        .ifPresent(s -> {
          List<Orderable> orderables = nonNull(vertice.getOrderables()) ? vertice.getOrderables() : new ArrayList<>();
          orderables.add((order.equalsIgnoreCase("desc")) ? s.desc() : s.asc());
          vertice.setOrderables(orderables);
        });
  }


  private RdfValue getDefaultOrderByValue(GraphQLFieldDefinition fieldDefinition) {
    GraphQLType type = GraphQLTypeUtil.unwrapOne(fieldDefinition.getType());

    if (NUMERIC_TYPES.stream()
        .anyMatch(numericType -> numericType.equals(type))) {
      return Rdf.literalOf(0);
    }

    return Rdf.literalOf("");
  }

  private Optional<Variable> findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<GraphQLFieldDefinition> fieldPath) {
    NodeShape childShape = getNextNodeShape(nodeShape, fieldPath);
    if (nodeShape.equals(childShape) || fieldPath.size() == 1) {
      return findOrCreatePath(vertice, query, nodeShape, fieldPath, false, false)
          .map(edge -> getSubjectForField(edge, nodeShape, fieldPath));

    } else {
      Edge edge = createSimpleEdge(query.var(), nodeShape.getPropertyShape(fieldPath.get(0)
          .getName())
          .getPath(), true, false);
      vertice.getEdges()
          .add(edge);
      final List<GraphQLFieldDefinition> subFieldPath = fieldPath.subList(1, fieldPath.size());
      return findOrCreatePath(edge.getObject(), query, childShape, subFieldPath, false, false)
          .map(e -> getSubjectForField(e, childShape, subFieldPath));
    }
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Optional<Edge> findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<GraphQLFieldDefinition> fieldPath, boolean required, boolean isVisible) {

    if (fieldPath.isEmpty()) {
      return Optional.empty();
    }

    PropertyShape propertyShape = nodeShape.getPropertyShape(fieldPath.get(0)
        .getName());
    Edge match = findOrCreateEdge(query, propertyShape, vertice, required, isVisible);
    if (required) {
      match.setOptional(false);
    }

    NodeShape childShape = getNextNodeShape(nodeShape, fieldPath);

    if (fieldPath.size() == 1) {
      createAggregate(fieldPath.get(0), query.var()).ifPresent(match::setAggregate);

      return Optional.of(match);
    }

    return findOrCreatePath(match.getObject(), query, childShape, fieldPath.subList(1, fieldPath.size()), required,
        isVisible);
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

  Optional<Aggregate> createAggregate(GraphQLFieldDefinition fieldDefinition, Variable variable) {
    return Optional.ofNullable(fieldDefinition)
        .map(field -> field.getDirective(Rdf4jDirectives.AGGREGATE_NAME))
        .map(dir -> dir.getArgument(CoreInputTypes.AGGREGATE_TYPE))
        .map(argument -> argument.getValue()
            .toString())
        .map(AggregateType::valueOf)
        .map(aggregateType -> Aggregate.builder()
            .type(aggregateType)
            .variable(variable)
            .build());
  }
}
