package org.dotwebstack.framework.core.datafetchers;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeUtil;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.springframework.stereotype.Component;

@Component
public class TransformDirectiveDataFetcher extends DelegateDataFetcher {

  private final JexlEngine jexlEngine;

  public TransformDirectiveDataFetcher(final JexlEngine jexlEngine) {
    this.jexlEngine = jexlEngine;
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return environment.getField().getDirectives()
        .stream()
        .anyMatch(directive -> directive.getName().equals(CoreDirectives.TRANSFORM_NAME));
  }

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    boolean isListType = GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));

    Object value = getDelegate(environment).get(environment);

    if (value == null) {
      return null;
    }

    JexlExpression expression = getJexlExpression(fieldDefinition);

    if (isListType) {
      return ((Collection<?>) value).stream()
          .map(listItem -> expression.evaluate(createContext(environment, listItem)))
          .collect(Collectors.toList());
    }

    return expression.evaluate(createContext(environment, value));
  }

  private static JexlContext createContext(DataFetchingEnvironment environment, Object value) {
    return new MapContext(ImmutableMap.of(
        environment.getFieldDefinition().getName(), value));
  }

  private JexlExpression getJexlExpression(GraphQLFieldDefinition fieldDefinition) {
    return jexlEngine.createExpression(fieldDefinition.getDirective(CoreDirectives.TRANSFORM_NAME)
        .getArgument(CoreDirectives.TRANSFORM_ARG_EXPR).getValue().toString());
  }
}
