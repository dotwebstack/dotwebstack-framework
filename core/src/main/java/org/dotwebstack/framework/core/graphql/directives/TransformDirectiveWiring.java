package org.dotwebstack.framework.core.graphql.directives;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactories;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class TransformDirectiveWiring implements SchemaDirectiveWiring {

  private final JexlEngine jexlEngine;

  @Override
  public GraphQLFieldDefinition onField(
      SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldsContainer parentType = environment.getFieldsContainer();
    GraphQLFieldDefinition fieldDefinition = environment.getElement();

    if (!GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(fieldDefinition.getType()))) {
      throw new InvalidConfigurationException(
          "Directive @transform can only be used with (a list of) scalar fields.");
    }

    boolean isListType = GraphQLTypeUtil.isList(
        GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));

    JexlExpression expression = jexlEngine.createExpression(DirectiveUtils
        .getStringArgument(CoreDirectives.TRANSFORM_ARG_EXPR, environment.getDirective()));

    DataFetcher delegateDataFetcher = environment
        .getCodeRegistry()
        .getDataFetcher(parentType, fieldDefinition);

    DataFetcher wrappedDataFetcher = DataFetcherFactories
        .wrapDataFetcher(delegateDataFetcher, (delegateEnv, value) -> {
          if (value == null) {
            return null;
          }

          if (isListType) {
            return ((Collection<?>) value).stream()
                .map(listItem -> expression.evaluate(createContext(delegateEnv, listItem)))
                .collect(Collectors.toList());
          }

          return expression.evaluate(createContext(delegateEnv, value));
        });

    environment.getCodeRegistry()
        .dataFetcher(parentType, fieldDefinition, wrappedDataFetcher);

    return fieldDefinition;
  }

  private static JexlContext createContext(DataFetchingEnvironment environment, Object value) {
    return new MapContext(ImmutableMap.of(
        environment.getFieldDefinition().getName(), value));
  }

}
