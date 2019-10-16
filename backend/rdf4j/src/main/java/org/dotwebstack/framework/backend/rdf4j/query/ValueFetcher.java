package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.helper.CompareHelper.getComparator;
import static org.dotwebstack.framework.backend.rdf4j.helper.MemStatementListHelper.listOf;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sail.memory.model.MemResource;
import org.springframework.stereotype.Component;

@Component
public final class ValueFetcher extends SourceDataFetcher {

  private final NodeShapeRegistry nodeShapeRegistry;

  public ValueFetcher(final NodeShapeRegistry nodeShapeRegistry, Rdf4jConverterRouter router) {
    super(router);
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    PropertyShape propertyShape = getPropertyShape(environment);

    if (GraphQLTypeUtil.isList(fieldType)) {
      return resolve(environment, propertyShape, source).map(value -> convert(source.getModel(), propertyShape, value))
          .collect(Collectors.toList());
    }

    if (GraphQLTypeUtil.isScalar(fieldType) || fieldType instanceof GraphQLObjectType) {
      return resolve(environment, propertyShape, source).map(value -> convert(source.getModel(), propertyShape, value))
          .findFirst()
          .orElse(null);
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private PropertyShape getPropertyShape(DataFetchingEnvironment environment) {
    if (environment.getParentType() instanceof GraphQLObjectType) {
      NodeShape nodeShape = nodeShapeRegistry.getByShaclName(environment.getParentType()
          .getName());

      return nodeShape.getPropertyShape(environment.getField()
          .getName());
    }
    throw unsupportedOperationException("Cannot determine property shape for parent type '{}'.",
        environment.getParentType()
            .getClass()
            .getSimpleName());
  }

  private Stream<Value> resolve(DataFetchingEnvironment environment, PropertyShape propertyShape,
      QuerySolution source) {
    Stream<Value> stream = propertyShape.getPath()
        .resolvePath(source.getModel(), source.getSubject())
        .stream()
        .filter(result -> {
          if (propertyShape.getNode() != null) {
            if (result instanceof SimpleIRI) {
              return true;
            }

            return resultIsOfType(result, propertyShape.getNode()
                .getTargetClasses());
          }
          return true;
        });

    GraphQLArgument sortArgument = environment.getFieldDefinition()
        .getArgument(CoreDirectives.SORT_NAME);
    if (Objects.nonNull(sortArgument)
        && GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {
      boolean asc = Objects.equals("ASC", ((Map) ((List) sortArgument.getDefaultValue()).get(0)).get("order")
          .toString());

      if (GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(environment.getFieldType()))) {
        return stream.sorted(getComparator(asc));
      }

      if (Objects.nonNull(propertyShape.getNode())) {
        String field = environment.getFieldDefinition()
            .getName();
        if (Objects.nonNull(((Map) ((List) sortArgument.getDefaultValue()).get(0)).get("field"))) {
          field = ((Map) ((List) sortArgument.getDefaultValue()).get(0)).get("field")
              .toString();
        }
        return stream.sorted(getComparator(asc, source.getModel(), field, propertyShape.getNode()));
      }
    }

    return stream;
  }

  private boolean resultIsOfType(Value value, Set<IRI> types) {
    return listOf(((MemResource) value).getSubjectStatementList()).stream()
        .anyMatch(statement -> statement.getPredicate()
            .equals(RDF.TYPE)
            && types.stream()
                .anyMatch(type -> statement.getObject()
                    .equals(type)));
  }

  private Object convert(@NonNull Model model, @NonNull PropertyShape propertyShape, @NonNull Value value) {
    if (propertyShape.getNode() != null || BNode.class.isAssignableFrom(value.getClass())) {
      return new QuerySolution(model, (Resource) value);
    }

    return this.converterRouter.convert(value);
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return (environment.getSource() instanceof QuerySolution);
  }
}
