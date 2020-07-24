package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.helper.CompareHelper.getComparator;
import static org.dotwebstack.framework.backend.rdf4j.helper.MemStatementListHelper.listOf;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_ORDER;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_ORDER_ASC;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sail.memory.model.MemResource;

public final class ValueFetcher extends SourceDataFetcher {

  private final NodeShapeRegistry nodeShapeRegistry;

  public ValueFetcher(final NodeShapeRegistry nodeShapeRegistry, Rdf4jConverterRouter router) {
    super(router);
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    QuerySolution source = environment.getSource();

    if (isResource(environment.getFieldDefinition())) {
      return source.getSubject();
    }

    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    PropertyShape propertyShape = getPropertyShape(environment);

    if (GraphQLTypeUtil.isList(fieldType)) {
      return getResolvedValues(environment, source, propertyShape).collect(Collectors.toList());
    }

    if (GraphQLTypeUtil.isScalar(fieldType) || fieldType instanceof GraphQLObjectType) {
      return getResolvedValues(environment, source, propertyShape).findFirst()
          .orElse(null);
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private boolean isResource(GraphQLFieldDefinition fieldDefinition) {
    return Objects.nonNull(fieldDefinition.getDirective(Rdf4jDirectives.RESOURCE_NAME));
  }

  private Stream<Object> getResolvedValues(DataFetchingEnvironment environment, QuerySolution source,
      PropertyShape propertyShape) {
    return resolve(environment, propertyShape, source).map(value -> convert(source.getModel(), propertyShape, value));
  }

  private PropertyShape getPropertyShape(DataFetchingEnvironment environment) {
    if (environment.getParentType() instanceof GraphQLObjectType) {
      NodeShape nodeShape = nodeShapeRegistry.getByShaclName(getTypeName(environment.getParentType()));

      return nodeShape.getPropertyShape(environment.getField()
          .getName());
    }
    throw unsupportedOperationException("Cannot determine property shape for parent type '{}'.",
        environment.getParentType()
            .getClass()
            .getSimpleName());
  }

  private Stream<Value> resolve(final DataFetchingEnvironment environment, PropertyShape propertyShape,
      QuerySolution source) {
    NodeShape nodeShape = propertyShape.getNode();
    Set<Value> values = propertyShape.getPath()
        .resolvePath(source.getModel(), source.getSubject());
    Stream<Value> stream = values.stream()
        .filter(result -> hasValidPropertyShapeConstraints(result, nodeShape, propertyShape, source.getModel(),
            new HashSet<String>()))
        .filter(result -> nodeShape == null || nodeShape.getClasses()
            .isEmpty() || result instanceof SimpleLiteral
            || (result instanceof Resource
                ? resultIsOfType((Resource) result, source.getModel(), nodeShape.getClasses())
                : resultIsOfType(result, nodeShape.getClasses())));

    Optional<GraphQLArgument> sortArgumentOptional = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> Objects.nonNull(argument.getDirective(CoreDirectives.SORT_NAME)))
        .findFirst();

    if (GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))
        && sortArgumentOptional.isPresent()) {
      GraphQLArgument sortArgument = sortArgumentOptional.get();
      boolean asc = getSortOrder(sortArgument);

      if (GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(environment.getFieldType()))) {
        return stream.sorted(getComparator(asc));
      }

      if (Objects.nonNull(nodeShape)) {
        GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getFieldDefinition()
            .getType());
        return stream.sorted(getComparator(asc, source.getModel(), sortArgument, nodeShape, objectType));
      }
    }

    return stream;
  }

  @SuppressWarnings("rawtypes")
  private Boolean getSortOrder(GraphQLArgument sortArgument) {
    Map orderables = (Map) ((List) sortArgument.getDefaultValue()).get(0);
    Object fieldOrder = orderables.get(SORT_FIELD_ORDER);
    return Objects.equals(SORT_FIELD_ORDER_ASC, fieldOrder.toString());
  }

  private boolean resultIsOfType(Resource resource, Model model, Set<Set<IRI>> orTypes) {
    return orTypes.stream()
        .allMatch(andTypes -> andTypes.stream()
            .anyMatch(type -> model.filter(resource, RDF.TYPE, null)
                .stream()
                .anyMatch(statement -> statement.getObject()
                    .equals(type))));
  }


  private boolean resultIsOfType(Value value, Set<Set<IRI>> orTypes) {
    return orTypes.stream()
        .allMatch(andTypes -> andTypes.stream()
            .anyMatch(type -> listOf(((MemResource) value).getSubjectStatementList()).stream()
                .anyMatch(statement -> statement.getPredicate()
                    .equals(RDF.TYPE)
                    && statement.getObject()
                        .equals(type))));
  }

  private boolean hasValidPropertyShapeConstraints(Object value, NodeShape nodeShape, PropertyShape propertyShape,
      Model model, HashSet<String> checked) {

    checked.add(getKey(nodeShape, propertyShape));
    return propertyShape.getConstraints()
        .entrySet()
        .stream()
        .allMatch((entry) -> {
          boolean valid = hasValidConstraint(entry.getKey(), entry.getValue(), value);
          NodeShape targetNode = propertyShape.getNode();
          if (valid && value instanceof Resource && targetNode != null) {
            valid = hasValidNodeShapeConstraints((Resource) value, targetNode, model, checked);
          }
          return valid;
        });
  }

  private boolean hasValidNodeShapeConstraints(Resource subject, NodeShape nodeShape, Model model,
      HashSet<String> checked) {
    return nodeShape.getPropertyShapes()
        .values()
        .stream()
        .filter(propertyShape -> !checked.contains(getKey(nodeShape, propertyShape)))
        .allMatch(propertyShape -> {
          Set<Value> values = propertyShape.getPath()
              .resolvePath(model, subject);

          boolean valid;
          if (values.isEmpty()) {
            valid = hasValidPropertyShapeConstraints(null, nodeShape, propertyShape, model, checked);
          } else {
            valid = values.stream()
                .anyMatch(childValue -> hasValidPropertyShapeConstraints(childValue, nodeShape, propertyShape, model,
                    checked));
          }
          return valid;
        });
  }

  private String getKey(NodeShape nodeShape, PropertyShape propertyShape) {
    String nodeShapeKey = nodeShape != null ? nodeShape.getName() : "root";
    return nodeShapeKey + "_" + propertyShape.getName();
  }

  private boolean hasValidConstraint(ConstraintType type, Object constraintValue, Object value) {
    if (type == ConstraintType.HASVALUE) {
      return Objects.equals(Objects.toString(constraintValue), Objects.toString(value));
    }
    return true;
  }

  private Object convert(@NonNull Model model, @NonNull PropertyShape propertyShape, @NonNull Value value) {
    if ((propertyShape.getNode() != null || BNode.class.isAssignableFrom(value.getClass()))
        && !(value instanceof Literal)) {
      return new QuerySolution(model, (Resource) value);
    }

    return this.converterRouter.convertFromValue(value);
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return (environment.getSource() instanceof QuerySolution);
  }
}
