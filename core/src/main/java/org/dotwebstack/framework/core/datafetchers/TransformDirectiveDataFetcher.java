package org.dotwebstack.framework.core.datafetchers;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeUtil;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.scalars.CoreCoercing;
import org.springframework.stereotype.Component;

@Component
public class TransformDirectiveDataFetcher extends DelegateDataFetcher {

  private final JexlEngine jexlEngine;

  private final List<CoreCoercing<?>> coercings;

  public TransformDirectiveDataFetcher(final JexlEngine jexlEngine, final List<CoreCoercing<?>> coercings) {
    this.jexlEngine = jexlEngine;
    this.coercings = coercings;
  }

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return environment.getFieldDefinition()
        .getDirectives()
        .stream()
        .anyMatch(directive -> directive.getName()
            .equals(CoreDirectives.TRANSFORM_NAME));
  }

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    boolean isListType = GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));

    Object value = getDelegate(environment).get(environment);

    if (value == null) {
      return null;
    }

    Object parsedValue = parseValue(fieldDefinition, value);

    JexlExpression expression = getJexlExpression(fieldDefinition);

    if (isListType) {
      return ((Collection<?>) value).stream()
          .map(listItem -> expression.evaluate(createContext(environment, listItem)))
          .collect(Collectors.toList());
    }

    return expression.evaluate(createContext(environment, parsedValue));
  }

  private Object parseValue(GraphQLFieldDefinition fieldDefinition, Object value) {
    String transformType = (String) fieldDefinition.getDirective(CoreDirectives.TRANSFORM_NAME)
        .getArgument(CoreDirectives.TRANSFORM_ARG_TYPE)
        .getValue();

    // when type is null, it is implied that it is string, in those cases we do not coerce the value
    if (transformType != null && !transformType.equals("String")) {
      CoreCoercing<?> compatibleCoercing = coercings.stream()
          .filter(coercing -> coercing.isCompatible(transformType))
          .findFirst()
          .orElseThrow(() -> ExceptionHelper
              .unsupportedOperationException("Did not find a suitable Coercing for type '{}'", transformType));

      return compatibleCoercing.serialize(value);
    }

    return value;
  }

  private static JexlContext createContext(DataFetchingEnvironment environment, Object value) {
    return new MapContext(ImmutableMap.of(environment.getFieldDefinition()
        .getName(), value));
  }

  private JexlExpression getJexlExpression(GraphQLFieldDefinition fieldDefinition) {
    return jexlEngine.createExpression(fieldDefinition.getDirective(CoreDirectives.TRANSFORM_NAME)
        .getArgument(CoreDirectives.TRANSFORM_ARG_EXPR)
        .getValue()
        .toString());
  }
}
