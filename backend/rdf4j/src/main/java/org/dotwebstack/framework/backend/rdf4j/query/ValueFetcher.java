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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    LOG.debug("filtering propertyshape {} values {}", propertyShape.getPath()
        .toPredicate()
        .toString(), values);
    Stream<Value> stream = values.stream()
        .filter(result -> constraintCheck(result, propertyShape, source.getModel()))
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

  private boolean resultIsOfType(Resource resource, Model model, Set<IRI> types) {
    return model.filter(resource, RDF.TYPE, null)
        .stream()
        .anyMatch(statement -> types.stream()
            .anyMatch(type -> statement.getObject()
                .equals(type)));
  }


  private boolean resultIsOfType(Value value, Set<IRI> types) {
    return listOf(((MemResource) value).getSubjectStatementList()).stream()
        .anyMatch(statement -> statement.getPredicate()
            .equals(RDF.TYPE)
            && types.stream()
                .anyMatch(type -> statement.getObject()
                    .equals(type)));
  }

  private boolean constraintCheck(Object value, PropertyShape propertyShape, Model model) {
    LOG.debug("checking contraints on propertyShape {} with value {} ", propertyShape.getPath()
        .toPredicate()
        .getQueryString(), value);

    return propertyShape.getConstraints()
        .entrySet()
        .stream()
        .allMatch((entry) -> {
          boolean valid = isValid(entry.getKey(), entry.getValue(), value, propertyShape, model);
          NodeShape targetNode = propertyShape.getNode();
          if (valid && value instanceof Resource && targetNode != null) {
            LOG.debug("{} is pointing to node {}, checking child values", value, targetNode.getClasses());
            return targetNode.getPropertyShapes()
                .values()
                .stream()
                .allMatch(childPs -> {
                  Set<Value> childValues = childPs.getPath()
                      .resolvePath(model, (Resource) value);
                  LOG.debug("got values for targetNode {} with value {} and propertyShape {}: {} ",
                      targetNode.getClasses(), value, childPs.getPath()
                          .toPredicate()
                          .getQueryString(),
                      childValues);
                  return childValues.stream()
                      .allMatch(cv -> constraintCheck(cv, childPs, model));
                });
          }
          return valid;
        });
  }

  private boolean isValid(ConstraintType type, Object constraintValue, Object value, PropertyShape propertyShape,
      Model model) {
    LOG.debug("checking contraint {}={} for value {}", type, constraintValue, value);
    switch (type) {
      case HASVALUE:
        return Objects.equals(Objects.toString(constraintValue), Objects.toString(value));
      case MAXCOUNT:
      case MINCOUNT:
      case RDF_TYPE:
      default:
        return true;
    }
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
