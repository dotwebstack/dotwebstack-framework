package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

@RequiredArgsConstructor
public final class ValueFetcher implements DataFetcher<Object> {

  private final PropertyShape propertyShape;

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    if (GraphQLTypeUtil.isList(fieldType)) {
      return resolve(source).collect(Collectors.toList());
    }

    if (GraphQLTypeUtil.isScalar(fieldType) || fieldType instanceof GraphQLObjectType) {
      return resolve(source).findFirst().orElse(null);
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private Stream<Object> resolve(QuerySolution source) {
    return propertyShape.getPath().resolvePath(source.getModel(), source.getSubject(), false)
        .stream()
        .map(value -> convert(source.getModel(),value));
  }

  private Object convert(@NonNull Model model, @NonNull Value value) {
    if (value instanceof Resource) {
      return new QuerySolution(model,(Resource) value);
    }

    if (value instanceof Literal) {
      return ValueUtils.convertValue(value);
    }

    throw unsupportedOperationException("Value of type '{}' is not supported!",value.getClass().getSimpleName());
  }
}
