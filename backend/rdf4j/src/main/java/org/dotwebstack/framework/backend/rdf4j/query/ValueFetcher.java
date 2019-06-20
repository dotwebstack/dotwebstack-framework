package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.helper.QuerySolutionHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.datafetchers.SourceDataFetcher;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.stereotype.Component;

@Component
public final class ValueFetcher extends SourceDataFetcher {

  private final NodeShapeRegistry nodeShapeRegistry;

  public ValueFetcher(final NodeShapeRegistry nodeShapeRegistry) {
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    PropertyShape propertyShape = getPropertyShape(environment);

    if (GraphQLTypeUtil.isList(fieldType)) {
      return resolve(propertyShape, source).collect(Collectors.toList());
    }

    if (GraphQLTypeUtil.isScalar(fieldType) || fieldType instanceof GraphQLObjectType) {
      return resolve(propertyShape, source).findFirst()
          .orElse(null);
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private PropertyShape getPropertyShape(DataFetchingEnvironment environment) {
    if (environment.getParentType() instanceof GraphQLObjectType) {
      return nodeShapeRegistry.get((GraphQLObjectType) environment.getParentType())
          .getPropertyShape(environment.getField()
              .getName());
    }
    throw ExceptionHelper.unsupportedOperationException("Cannot determine property shape for parent type '{}'.",
        environment.getParentType()
            .getClass()
            .getSimpleName());
  }

  private Stream<Object> resolve(PropertyShape propertyShape, QuerySolution source) {
    return propertyShape.getPath()
        .resolvePath(source.getModel(), source.getSubject(), false)
        .stream()
        .map(value -> convert(source.getModel(), value))
        .filter(result -> {
          if (propertyShape.getNode() != null) {
            return resultIsOfType((QuerySolution) result, propertyShape.getNode()
                .getTargetClass());
          }
          return true;
        });
  }

  private boolean resultIsOfType(@NonNull QuerySolution result, @NonNull IRI type) {
    return QuerySolutionHelper.getSubjectStatements(result)
        .stream()
        .anyMatch(statement -> statement.getPredicate()
            .equals(RDF.TYPE)
            && statement.getObject()
                .equals(type));
  }

  private Object convert(@NonNull Model model, @NonNull Value value) {
    if (value instanceof Resource) {
      return new QuerySolution(model, (Resource) value);
    }

    if (value instanceof Literal) {
      return ValueUtils.convertValue(value);
    }

    throw unsupportedOperationException("Value of type '{}' is not supported!", value.getClass()
        .getSimpleName());
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return (environment.getSource() instanceof QuerySolution);
  }
}
