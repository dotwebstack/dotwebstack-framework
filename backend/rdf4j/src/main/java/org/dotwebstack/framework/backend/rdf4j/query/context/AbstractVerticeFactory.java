package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.createSimpleEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.getNewEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.hasEqualQueryString;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.hasEqualTargetClass;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FieldPathHelper.getFieldPath;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.getOperand;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getSubjectForField;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.input.CoreInputTypes;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
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

  List<ArgumentResultWrapper> getArgumentFieldMapping(List<SelectedField> selectedFields, String directiveName) {
    return selectedFields.stream()
        .filter(selectedField -> !selectedField.getQualifiedName()
            .contains("/"))
        .flatMap(selectedField -> selectedField.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> argument.getDirective(directiveName) != null)
            .map(argument -> ArgumentResultWrapper.builder()
                .selectedField(selectedField)
                .argument(argument)
                .fieldPath(getFieldPath(selectedField, argument, directiveName))
                .build()))
        .collect(Collectors.toList());
  }

  void processSort(Vertice vertice, OuterQuery<?> query, PropertyShape propertyShape,
      ArgumentResultWrapper argumentResultWrapper) {

    FieldPath fieldPath = argumentResultWrapper.getFieldPath();
    if (!fieldPath.isResource()) {
      findOrCreatePath(vertice, propertyShape.getNode(), fieldPath, true, false, query);
    }
  }

  void addFilterToVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    if (filterRule.getFieldPath()
        .isResource()) {
      addFilterToVertice(nodeShape, vertice, filterRule, vertice.getSubject());
      return;
    }
    findOrCreatePath(vertice, nodeShape, filterRule.getFieldPath(), true, query).ifPresent(match -> {
      addFilterToVertice(nodeShape, match, filterRule);
    });
  }

  private void addFilterToVertice(NodeShape nodeShape, Edge edge, FilterRule filterRule) {
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
    String field = filterRule.getFieldPath()
        .last()
        .map(GraphQLFieldDefinition::getName)
        .orElse(null);
    return filterArgument -> filterRule.getFieldPath()
        .isResource() ? Rdf.iri(serializerRouter.serialize(filterArgument))
            : getOperand(nodeShape, field, serializerRouter.serialize(filterArgument), language);
  }

  private Stream<Object> getFilterArguments(Object filterValue) {
    return filterValue instanceof List ? castToList(filterValue).stream() : Stream.of(filterValue);
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Optional<Edge> findOrCreatePath(Vertice vertice, NodeShape nodeShape, FieldPath fieldPath,
      boolean makePathRequired, OuterQuery<?> query) {
    return findOrCreatePath(vertice, nodeShape, fieldPath, false, makePathRequired, query);
  }

  /*
   * Find the path to apply the filter on. In case no or a only partial path is found, create the part
   * of the path that does not yet exist
   */
  private Optional<Edge> findOrCreatePath(Vertice vertice, NodeShape nodeShape, FieldPath fieldPath, boolean isVisible,
      boolean makePathRequired, OuterQuery<?> query) {

    if (fieldPath.last()
        .map(fieldDefinition -> fieldDefinition.getDirective(Rdf4jDirectives.RESOURCE_NAME))
        .isPresent()) {
      return Optional.empty();
    }

    if (Objects.isNull(nodeShape)) {
      return Optional.empty();
    }

    Edge match =
        findOrCreateEdge(nodeShape.getPropertyShape(FieldPathHelper.getFirstName(fieldPath.getFieldDefinitions())),
            vertice, fieldPath.isRequired(), isVisible, query);

    if (makePathRequired) {
      match.setOptional(false);
    }

    if (fieldPath.isSingleton()) {
      createAggregate(fieldPath.first(), query.var()).ifPresent(match::setAggregate);
      return of(match);
    }

    return findOrCreatePath(match.getObject(), getNextNodeShape(nodeShape, fieldPath.getFieldDefinitions()),
        fieldPath.rest()
            .orElseThrow(() -> illegalStateException("Remainder expected but got nothing!")),
        isVisible, false, query);
  }

  /*
   * Find the edge belonging to the given propertyshape. In case no propertyshape is found, create a
   * new one
   */
  private Edge findOrCreateEdge(PropertyShape propertyShape, Vertice vertice, boolean required, boolean isVisible,
      OuterQuery<?> query) {
    List<Edge> childEdges = nonNull(vertice.getEdges()) ? vertice.getEdges() : new ArrayList<>();

    Edge edge = childEdges.stream()
        .filter(childEdge -> hasEqualQueryString(childEdge, propertyShape))
        .filter(childEdge -> hasEqualTargetClass(childEdge.getObject(), propertyShape.getNode()))
        .findFirst()
        .orElseGet(() -> getNewEdge(propertyShape, vertice, isVisible, required, query));
    if (required) {
      edge.setOptional(false);
    }
    return edge;
  }

  void addOrderables(Vertice vertice, OuterQuery<?> query, OrderBy orderBy, NodeShape nodeShape) {
    Optional<Variable> subject;

    if (orderBy.getFieldPath()
        .last()
        .filter(leaf -> Objects.nonNull(leaf.getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .isPresent()) {
      subject = getSubjectForResource(vertice, nodeShape, orderBy.getFieldPath(), query);
    } else {
      subject = getSubject(vertice, nodeShape, orderBy.getFieldPath(), query);
    }

    subject.map(s -> Optional.of(orderBy.getFieldPath())
        .filter(fieldPath -> !fieldPath.isRequired())
        .flatMap(FieldPath::last)
        .map(fieldDefinition -> (Orderable) Expressions.coalesce(s, getDefaultOrderByValue(fieldDefinition)))
        .orElse(s))
        .ifPresent(s -> {
          List<Orderable> orderables = nonNull(vertice.getOrderables()) ? vertice.getOrderables() : new ArrayList<>();
          orderables.add((orderBy.getOrder()
              .equalsIgnoreCase("desc")) ? s.desc() : s.asc());
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

  static Optional<Constraint> getValueConstraint(PropertyShape propertyShape) {
    if (propertyShape.getMinCount() != null && propertyShape.getMinCount() >= 1
        && propertyShape.getHasValue() != null) {
      return Optional.of(Constraint.builder()
          .predicate(propertyShape.getPath()
              .toPredicate())
          .constraintType(ConstraintType.HASVALUE)
          .values(Set.of(propertyShape.getHasValue()))
          .build());
    } else {
      return Optional.empty();
    }
  }

  static Optional<Constraint> getMinCountConstraint(PropertyShape propertyShape, OuterQuery<?> outerQuery) {
    if (propertyShape.getMinCount() != null && propertyShape.getMinCount() >= 1) {
      return Optional.of(Constraint.builder()
          .predicate(propertyShape.getPath()
              .toPredicate())
          .constraintType(ConstraintType.MINCOUNT)
          .values(Set.of(outerQuery.var()))
          .build());
    } else {
      return Optional.empty();
    }
  }

  static Optional<Constraint> getTypeConstraint(NodeShape nodeShape) {
    Set<Object> classes = new HashSet<>(nodeShape.getClasses());
    if (!classes.isEmpty()) {
      return Optional.of(Constraint.builder()
          .constraintType(ConstraintType.RDF_TYPE)
          .predicate(() -> stringify(RDF.TYPE))
          .values(classes)
          .build());
    }
    return Optional.empty();
  }

  static void addRequiredEdges(Vertice vertice, Collection<PropertyShape> propertyShapes, OuterQuery<?> query) {
    propertyShapes.stream()
        .filter(ps -> ps.getMinCount() != null && ps.getMinCount() >= 1 && ps.getNode() != null)
        .forEach(ps -> {
          Edge simpleEdge = createSimpleEdge(query.var(), ps, true, false);

          vertice.getEdges()
              .add(simpleEdge);
          addRequiredEdges(simpleEdge.getObject(), ps.getNode()
              .getPropertyShapes()
              .values(), query);
        });
  }

  static List<Edge> getRequiredEdges(Collection<PropertyShape> propertyShapes, OuterQuery<?> query) {
    return propertyShapes.stream()
        .filter(ps -> ps.getMinCount() != null && ps.getMinCount() >= 1)
        .map(ps -> {
          Edge edge = createSimpleEdge(query.var(), ps, true, false);
          if (ps.getNode() != null) {
            addRequiredEdges(edge.getObject(), ps.getNode()
                .getPropertyShapes()
                .values(), query);
          }
          return edge;
        })
        .collect(Collectors.toList());
  }

  /*
   * Check which edges should be added to the where part of the query based on a sh:minCount property
   * of 1
   */
  static void addConstraints(@NonNull Vertice vertice, @NonNull OuterQuery<?> outerQuery) {
    getTypeConstraint(vertice.getNodeShape()).ifPresent(vertice.getConstraints()::add);
    vertice.getNodeShape()
        .getPropertyShapes()
        .values()
        .forEach(ps -> {
          getValueConstraint(ps).ifPresent(vertice.getConstraints()::add);
          getMinCountConstraint(ps, outerQuery).ifPresent(minCountConstraint -> {
            vertice.getConstraints()
                .add(minCountConstraint);
          });
        });

    vertice.getEdges()
        .stream()
        .filter(edge -> edge.getPropertyShape() != null)
        .forEach(edge -> {
          Vertice childVertice = edge.getObject();
          NodeShape childNodeShape = childVertice.getNodeShape();
          if (childNodeShape != null) {
            addConstraints(childVertice, outerQuery);
          }
        });
  }

  private Optional<Variable> getSubjectForResource(Vertice vertice, NodeShape nodeShape, FieldPath fieldPath,
      OuterQuery<?> query) {
    return fieldPath.rest()
        .flatMap(remainder -> findOrCreatePath(vertice, nodeShape, remainder, false, query)
            .map(edge -> getSubjectForField(edge, nodeShape, remainder)))
        .or(() -> Optional.of(vertice.getSubject()));
  }

  private Optional<Variable> getSubject(Vertice vertice, NodeShape nodeShape, final FieldPath fieldPath,
      OuterQuery<?> query) {
    NodeShape childShape = getNextNodeShape(nodeShape, fieldPath.getFieldDefinitions());
    if (nodeShape.equals(childShape) || fieldPath.isSingleton()) {
      return findOrCreatePath(vertice, nodeShape, fieldPath, false, query)
          .map(edge -> getSubjectForField(edge, nodeShape, fieldPath));
    }

    Edge simpleEdge = createSimpleEdge(query.var(), nodeShape.getPropertyShape(fieldPath.first()
        .getName()), false, !fieldPath.isRequired());

    vertice.getEdges()
        .add(simpleEdge);

    return fieldPath.rest()
        .flatMap(remainder -> findOrCreatePath(simpleEdge.getObject(), childShape, remainder, false, query)
            .map(edge -> getSubjectForField(edge, childShape, remainder)));
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
