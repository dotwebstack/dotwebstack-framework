package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dotwebstack.framework.backend.rdf4j.helper.FieldPathHelper.getFieldDefinitions;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getOperand;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getSubjectForField;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasChildEdgeOfType;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_FIELD;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_IS_RESOURCE;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_ORDER;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.helper.FieldPathHelper;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.core.directives.CoreDirectives;
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
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;

@Slf4j
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
    return buildEdge(basePath.toPredicate(), basePath.toConstructPredicate(), buildObject(subject, new HashSet<>()),
        isVisible, isOptional);
  }

  Edge createSimpleEdge(Variable subject, Set<Iri> iris, RdfPredicate predicate, boolean isVisible) {
    return buildEdge(predicate, null, buildObject(subject, iris), isVisible, false);
  }

  Edge buildEdge(RdfPredicate predicate, RdfPredicate constructPredicate, Vertice object, boolean isVisible,
      boolean isOptional) {
    return Edge.builder()
        .predicate(predicate)
        .constructPredicate(constructPredicate)
        .object(object)
        .isVisible(isVisible)
        .isOptional(isOptional)
        .build();
  }

  private Vertice buildObject(Variable subject, Set<Iri> iris) {
    return Vertice.builder()
        .subject(subject)
        .iris(iris)
        .build();
  }

  Map<GraphQLArgument, ArgumentResultWrapper> getArgumentFieldMapping(NodeShape nodeShape, List<SelectedField> fields,
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
            .map(argument -> new AbstractMap.SimpleEntry<>(argument,
                getArgumentResultWrapper(field, argument, directiveName))))
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  private ArgumentResultWrapper getArgumentResultWrapper(SelectedField selectedField, GraphQLArgument argument,
      String directiveName) {
    return ArgumentResultWrapper.builder()
        .selectedField(selectedField)
        .argument(argument)
        .fieldPath(getFieldPath(selectedField, argument, directiveName))
        .build();
  }

  private String getFieldName(GraphQLArgument argument, String directiveName) {
    if (CoreDirectives.SORT_NAME.equals(directiveName)) {
      Object fieldValue = nonNull(argument.getValue()) ? argument.getValue() : argument.getDefaultValue();
      Map<String, Object> map = nonNull(fieldValue) ? castToMap(((List<Object>) fieldValue).get(0)) : emptyMap();

      return map.getOrDefault("field", argument.getName())
          .toString();
    }

    GraphQLDirective directive = argument.getDirective(directiveName);
    if (nonNull(directive)) {
      String fieldName = DirectiveUtils.getArgument(directive, "field", String.class);

      if (isNotBlank(fieldName)) {
        return fieldName;
      }

      return argument.getName();
    }

    throw illegalStateException("Could not find directive for argument {} and directiveName {}", argument.getName(),
        directiveName);
  }

  private List<GraphQLFieldDefinition> getFieldPath(SelectedField selectedField, GraphQLArgument argument,
      String directiveName) {
    GraphQLUnmodifiedType unmodifiedType = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
        .getType());
    String fieldName = getFieldName(argument, directiveName);

    if (unmodifiedType instanceof GraphQLObjectType) {
      GraphQLObjectType objectType = (GraphQLObjectType) unmodifiedType;

      return getFieldDefinitions(objectType, fieldName);
    }

    if (unmodifiedType instanceof GraphQLScalarType) {
      return singletonList(selectedField.getFieldDefinition());
    }

    throw unsupportedOperationException("Unable to determine fieldDefinition for argument {}", argument);
  }

  List<Edge> findEdgesToBeProcessed(NodeShape nodeShape, SelectedField field, List<Edge> edges) {
    return edges.stream()
        .filter(hasEqualQueryString(nodeShape.getPropertyShape(field.getName())))
        .filter(isScalarOrHasChildOfType(nodeShape, field))
        .collect(Collectors.toList());
  }

  private Predicate<Edge> isScalarOrHasChildOfType(NodeShape nodeShape, SelectedField field) {
    return edge -> GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(field.getFieldDefinition()
        .getType())) || hasChildEdgeOfType(edge, nodeShape.getPropertyShape(field.getName())
            .getNode()
            .getTargetClasses());
  }

  @SuppressWarnings({"unchecked"})
  void processSort(Vertice vertice, OuterQuery<?> query, PropertyShape propertyShape,
      ArgumentResultWrapper argumentResultWrapper) {

    if (argumentResultWrapper.getFieldPath()
        .size() > 0) {

      if (of(argumentResultWrapper.getFieldPath()
          .get(argumentResultWrapper.getFieldPath()
              .size() - 1)).map(fieldDefinition -> fieldDefinition.getDirective(Rdf4jDirectives.RESOURCE_NAME))
                  .isEmpty()) {
        findOrCreatePath(vertice, query, propertyShape.getNode(), argumentResultWrapper.getFieldPath(), false, true);
      }
    }
  }

  void processFilters(Vertice vertice, OuterQuery<?> query, PropertyShape propertyShape,
      ArgumentResultWrapper argumentResultWrapper) {

    Object value = argumentResultWrapper.getSelectedField()
        .getArguments()
        .get(argumentResultWrapper.getArgument()
            .getName());

    if (nonNull(value)) {
      FilterRule filterRule = FilterRule.builder()
          .path(argumentResultWrapper.getFieldPath())
          .value(value)
          .build();

      addFilterToVertice(vertice, query, propertyShape.getNode(), filterRule);
    }
  }

  void addFilterToVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    findOrCreatePath(vertice, query, nodeShape, filterRule.getPath(), true, false)
        .ifPresent(match -> addFilterToVertice(nodeShape, match, filterRule));
  }

  void addFilterToVertice(NodeShape nodeShape, Edge edge, FilterRule filterRule) {
    addFilterToVertice(nodeShape, edge.getObject(), filterRule, null);
  }

  void addFilterToVertice(NodeShape nodeShape, Vertice vertice, FilterRule filterRule, Variable variable) {
    List<Filter> filters = vertice.getFilters();

    Filter filter = createFilter(nodeShape, filterRule);
    filter.setVariable(variable);

    filters.add(filter);
  }

  /*
   * Create a new filter with either one argument or a list of arguments
   */
  private Filter createFilter(NodeShape nodeShape, FilterRule filterRule) {
    List<Operand> operands = getOperands(nodeShape, filterRule);

    FilterOperator operator = FilterOperator.getByValue(filterRule.getOperator())
        .orElse(FilterOperator.EQ);

    return Filter.builder()
        .operator(operator)
        .operands(operands)
        .build();
  }

  private List<Operand> getOperands(NodeShape nodeShape, FilterRule filterRule) {
    String language = rdf4jProperties.getShape()
        .getLanguage();

    return getFilterArguments(filterRule.getValue()).map(argumentToOperand(nodeShape, filterRule, language))
        .collect(Collectors.toList());
  }

  private Function<Object, Operand> argumentToOperand(NodeShape nodeShape, FilterRule filterRule, String language) {
    String field = filterRule.getPath()
        .get(filterRule.getPath()
            .size() - 1)
        .getName();
    return filterArgument -> filterRule.isResource() ? Rdf.iri(serializerRouter.serialize(filterArgument))
        : getOperand(nodeShape, field, serializerRouter.serialize(filterArgument), language);
  }

  private Stream<Object> getFilterArguments(Object filterValue) {
    return filterValue instanceof List ? castToList(filterValue).stream() : Stream.of(filterValue);
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Optional<Edge> findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<GraphQLFieldDefinition> fieldPath) {
    return findOrCreatePath(vertice, query, nodeShape, fieldPath, false, false);
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Optional<Edge> findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<GraphQLFieldDefinition> fieldPath, boolean required, boolean isVisible) {
    Edge match = findOrCreateEdge(query, nodeShape.getPropertyShape(FieldPathHelper.getFirstName(fieldPath)), vertice,
        required, isVisible);

    if (fieldPath.size() == 1) {
      createAggregate(fieldPath.get(0), query.var()).ifPresent(match::setAggregate);
      return of(match);
    }

    return findOrCreatePath(match.getObject(), query, getNextNodeShape(nodeShape, fieldPath),
        fieldPath.subList(1, fieldPath.size()), required, isVisible);
  }

  /*
   * Find the edge belonging to the given propertyshape. In case no propertyshape is found, create a
   * new one
   */
  private Edge findOrCreateEdge(OuterQuery<?> query, PropertyShape propertyShape, Vertice vertice, boolean required,
      boolean isVisible) {
    List<Edge> childEdges = nonNull(vertice.getEdges()) ? vertice.getEdges() : new ArrayList<>();

    Edge edge = childEdges.stream()
        .filter(hasEqualQueryString(propertyShape))
        .filter(hasEqualTargetClass(propertyShape))
        .findFirst()
        .orElseGet(getNewEdge(query, propertyShape, vertice, required, isVisible));
    if (required) {
      edge.setOptional(false);
    }
    return edge;
  }

  private Predicate<Edge> hasEqualQueryString(PropertyShape propertyShape) {
    return childEdge -> {
      String queryString = propertyShape.getPath()
          .toPredicate()
          .getQueryString();
      return childEdge.getPredicate()
          .getQueryString()
          .equals(queryString);
    };
  }

  private Predicate<Edge> hasEqualTargetClass(PropertyShape propertyShape) {
    return childEdge -> Objects.isNull(propertyShape.getNode()) || hasChildEdgeOfType(childEdge, propertyShape.getNode()
        .getTargetClasses());
  }

  private Supplier<Edge> getNewEdge(OuterQuery<?> query, PropertyShape propertyShape, Vertice vertice, boolean required,
      boolean isVisible) {
    Edge newEdge = createSimpleEdge(query.var(), propertyShape.getPath(), !required, isVisible);
    List<Edge> edges = vertice.getEdges();
    edges.add(newEdge);
    return () -> newEdge;
  }

  /*
   * It can happen that the same path is used twice, we want to overcome this problem, by looking at
   * the edges, if we find more edges with the same predicate, we place the child edges of the latter
   * edges we find, on top of the first edge we find.
   */
  void makeEdgesUnique(List<Edge> edges) {
    List<Edge> uniqueEdges = new ArrayList<>();
    edges.forEach(edge -> uniqueEdges.stream()
        .filter(hasEqualPredicate(edge))
        .findFirst()
        .ifPresentOrElse(addToDuplicate(edge), () -> uniqueEdges.add(edge)));
  }

  private Predicate<Edge> hasEqualPredicate(Edge edge) {
    return uniqueEdge -> uniqueEdge.getPredicate()
        .equals(edge.getPredicate());
  }

  private Consumer<Edge> addToDuplicate(Edge edge) {
    List<Edge> childEdges = edge.getObject()
        .getEdges();

    return duplicate -> duplicate.getObject()
        .getEdges()
        .addAll(childEdges);
  }

  void addOrderables(Vertice vertice, OuterQuery<?> query, Map<String, Object> orderMap, NodeShape nodeShape,
      GraphQLObjectType objectType) {
    String fieldName = getSortProperty(orderMap, SORT_FIELD_FIELD);
    String order = getSortProperty(orderMap, SORT_FIELD_ORDER);
    boolean isResource = Boolean.parseBoolean(getSortProperty(orderMap, SORT_FIELD_IS_RESOURCE));

    final List<GraphQLFieldDefinition> fieldPath = getFieldDefinitions(objectType, fieldName);

    Optional<Variable> subject;
    if (isResource) {
      subject = getSubjectForResource(vertice, query, nodeShape, fieldPath);
    } else {
      subject = getSubject(vertice, query, nodeShape, fieldPath);
    }

    subject.map(s -> Expressions.coalesce(s, getDefaultOrderByValue(fieldPath.get(fieldPath.size() - 1))))
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

  private Optional<Variable> getSubjectForResource(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      List<GraphQLFieldDefinition> fieldPath) {
    if (fieldPath.size() > 1) {
      List<GraphQLFieldDefinition> newFieldPath = fieldPath.subList(0, fieldPath.size() - 1);
      return findOrCreatePath(vertice, query, nodeShape, newFieldPath)
          .map(edge -> getSubjectForField(edge, nodeShape, newFieldPath));
    }
    return of(vertice.getSubject());
  }

  private Optional<Variable> getSubject(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape,
      final List<GraphQLFieldDefinition> fieldPath) {
    NodeShape childShape = getNextNodeShape(nodeShape, fieldPath);
    if (nodeShape.equals(childShape) || fieldPath.size() == 1) {
      return findOrCreatePath(vertice, query, nodeShape, fieldPath)
          .map(edge -> getSubjectForField(edge, nodeShape, fieldPath));
    }

    BasePath path = nodeShape.getPropertyShape(FieldPathHelper.getFirstName(fieldPath))
        .getPath();
    Edge simpleEdge = createSimpleEdge(query.var(), path, true, false);

    vertice.getEdges()
        .add(simpleEdge);

    List<GraphQLFieldDefinition> childFieldPath = fieldPath.subList(1, fieldPath.size());
    return findOrCreatePath(simpleEdge.getObject(), query, childShape, childFieldPath)
        .map(edge -> getSubjectForField(edge, childShape, childFieldPath));
  }

  private String getSortProperty(Map<String, Object> orderMap, String sortFieldField) {
    return (String) orderMap.get(sortFieldField);
  }

  void addLanguageFilter(Edge edge, PropertyShape propertyShape) {
    if (Objects.equals(RDF.LANGSTRING, propertyShape.getDatatype())) {
      edge.getObject()
          .getFilters()
          .add(createLanguageFilter());
    }
  }

  private Filter createLanguageFilter() {
    ImmutableList<Operand> operands = ImmutableList.of(Rdf.literalOf(rdf4jProperties.getShape()
        .getLanguage()));

    return Filter.builder()
        .operator(FilterOperator.LANGUAGE)
        .operands(operands)
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
