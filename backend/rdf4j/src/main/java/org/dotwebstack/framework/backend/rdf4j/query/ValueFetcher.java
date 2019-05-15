package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;

@RequiredArgsConstructor
public final class ValueFetcher implements DataFetcher<Object> {

  private final PropertyShape propertyShape;

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    if (GraphQLTypeUtil.isScalar(fieldType)) {
      return getScalar(source);
    }

    if (GraphQLTypeUtil.isList(fieldType)
        && GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapNonNull(((GraphQLList)fieldType).getWrappedType()))) {
      return getList(source);
    }

    throw unsupportedOperationException("Field type '{}' not supported.", fieldType);
  }

  private Object getScalar(QuerySolution source) {
    return propertyShape.getPath().resolvePath(source.getModel(), source.getSubject(), false)
        .stream()
        .findFirst()
        .map(ValueUtils::convertValue)
        .orElse(null);
  }

  private Object getList(QuerySolution source) {
    return propertyShape.getPath().resolvePath(source.getModel(), source.getSubject(), false)
        .stream()
        .map(ValueUtils::convertValue)
        .collect(Collectors.toList());
  }
}
