package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getFilterRulePath;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getOperand;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getSubjectForField;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasChildEdgeOfType;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_FIELD;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_IS_RESOURCE;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_ORDER;

import com.google.common.collect.ImmutableList;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
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
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.OrderCondition;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Slf4j
abstract class AbstractVerticeFactory {

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

  Map<GraphQLArgument, SelectedField> getArgumentFieldMapping(NodeShape nodeShape, List<SelectedField> fields,
      String directiveName) {
    return fields.stream()
        .filter(isFieldNotPath())
        .filter(hasDeclaredProperyShape(nodeShape))
        .flatMap(field -> field.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> argument.getDirective(directiveName) != null)
            .map(argument -> new AbstractMap.SimpleEntry<>(argument, field)))
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  private Predicate<SelectedField> hasDeclaredProperyShape(NodeShape nodeShape) {
    return field -> Objects.nonNull(nodeShape.getPropertyShape(field.getName())
        .getNode());
  }

  private Predicate<SelectedField> isFieldNotPath() {
    return field -> !field.getQualifiedName()
        .contains("/");
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
  void processEdgeSort(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, NodeShape nodeShape,
      SelectedField field) {
    Object orderByList = Objects.nonNull(argument.getValue()) ? argument.getValue() : argument.getDefaultValue();
    Map<String, Object> orderMap = castToMap(((List<Object>) orderByList).get(0));

    String fieldName = orderMap.getOrDefault(SORT_FIELD_FIELD, argument.getName())
        .toString();

    orderMap.put(SORT_FIELD_IS_RESOURCE, isResource(field, fieldName));

    if (!(boolean) orderMap.get(SORT_FIELD_IS_RESOURCE)) {
      findOrCreatePath(vertice, query, nodeShape.getPropertyShape(field.getName())
          .getNode(), new ArrayList<>(Arrays.asList(fieldName.split("\\."))), false, true);
    }
  }

  private boolean isResource(SelectedField field, String fieldName) {
    return Optional.ofNullable(getField(field.getFieldDefinition(), fieldName.split("\\.")))
        .flatMap(definition -> Optional.ofNullable(definition.getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .isPresent();
  }

  private GraphQLFieldDefinition getField(GraphQLFieldDefinition environment, String[] path) {
    return path.length > 1 ? getField(environment, Arrays.copyOfRange(path, 1, path.length))
        : ((GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getType())).getFieldDefinition(path[0]);
  }

  void processEdge(Vertice vertice, GraphQLArgument argument, OuterQuery<?> query, NodeShape nodeShape,
      SelectedField field) {
    List<String> fieldPath = Arrays.asList(field.getName()
        .split("\\."));

    Optional.ofNullable(field.getArguments()
        .get(argument.getName()))
        .map(filterValue -> getFilterRule(argument, filterValue))
        .ifPresent(filter -> addFilterToVertice(vertice, query, getNextNodeShape(nodeShape, fieldPath), filter));
  }

  private FilterRule getFilterRule(GraphQLArgument argument, Object filterValue) {
    List<String> startPath = getFilterRulePath(argument);

    return FilterRule.builder()
        .path(startPath)
        .value(filterValue)
        .build();
  }

  void addFilterToVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    Vertice usedVertice = getVertice(vertice, query, nodeShape, filterRule);

    List<Filter> filters = Objects.nonNull(usedVertice.getFilters()) ? usedVertice.getFilters() : new ArrayList<>();
    filters.add(createFilter(nodeShape, filterRule));

    usedVertice.setFilters(filters);
  }

  private Vertice getVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    if (filterRule.isResource()) {
      return vertice.getEdges()
          .stream()
          .map(Edge::getObject)
          .findFirst()
          .orElse(vertice);
    } else {
      return findOrCreatePath(vertice, query, nodeShape, filterRule.getPath(), true, false).getObject();
    }
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
            .size() - 1);
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
  private Edge findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, List<String> fieldPaths) {
    return findOrCreatePath(vertice, query, nodeShape, fieldPaths, false, false);
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Edge findOrCreatePath(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, List<String> fieldPaths,
      boolean required, boolean isVisible) {
    Edge match = findOrCreateEdge(query, nodeShape.getPropertyShape(fieldPaths.get(0)), vertice, required, isVisible);

    return fieldPaths.size() == 1 ? match
        : findOrCreatePath(match.getObject(), query, getNextNodeShape(nodeShape, fieldPaths),
            fieldPaths.subList(1, fieldPaths.size()), required, isVisible);
  }

  /*
   * Find the edge belonging to the given propertyshape. In case no propertyshape is found, create a
   * new one
   */
  private Edge findOrCreateEdge(OuterQuery<?> query, PropertyShape propertyShape, Vertice vertice, boolean required,
      boolean isVisible) {
    List<Edge> childEdges = Objects.nonNull(vertice.getEdges()) ? vertice.getEdges() : new ArrayList<>();

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

  void addOrderables(Vertice vertice, OuterQuery<?> query, Map<String, Object> orderMap, NodeShape nodeShape) {
    String fieldName = getSortProperty(orderMap, SORT_FIELD_FIELD);
    String order = getSortProperty(orderMap, SORT_FIELD_ORDER);
    boolean isResource = Boolean.parseBoolean(getSortProperty(orderMap, SORT_FIELD_IS_RESOURCE));

    List<String> fieldPaths = Arrays.asList(fieldName.split("\\."));

    Variable subject = isResource ? getSubjectForResource(vertice, query, nodeShape, fieldName, fieldPaths)
        : getSubject(vertice, query, nodeShape, fieldPaths);
    OrderCondition orderCondition =
        (Objects.isNull(order) || order.equalsIgnoreCase("desc")) ? subject.desc() : subject.asc();

    List<Orderable> orderables = Objects.nonNull(vertice.getOrderables()) ? vertice.getOrderables() : new ArrayList<>();

    orderables.add(Expressions.custom(() -> "", Expressions.not(Expressions.bound(subject))));
    orderables.add(orderCondition);
    vertice.setOrderables(orderables);
  }

  private Variable getSubjectForResource(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, String fieldName,
      List<String> fieldPaths) {
    if (fieldName.contains(".")) {
      fieldPaths = fieldPaths.subList(0, fieldPaths.size() - 1);
      Edge match = findOrCreatePath(vertice, query, nodeShape, fieldPaths);
      return getSubjectForField(match, nodeShape, fieldPaths);
    }
    return vertice.getSubject();
  }

  private Variable getSubject(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, List<String> fieldPaths) {
    NodeShape childShape = getNextNodeShape(nodeShape, fieldPaths);
    if (nodeShape.equals(childShape)) {
      Edge match = findOrCreatePath(vertice, query, nodeShape, fieldPaths);
      return getSubjectForField(match, nodeShape, fieldPaths);
    }

    BasePath path = nodeShape.getPropertyShape(fieldPaths.get(0))
        .getPath();
    Edge edge = createSimpleEdge(query.var(), path, true, false);

    vertice.getEdges()
        .add(edge);

    fieldPaths = fieldPaths.subList(1, fieldPaths.size());
    Edge match = findOrCreatePath(edge.getObject(), query, childShape, fieldPaths);

    return getSubjectForField(match, childShape, fieldPaths);
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
}
