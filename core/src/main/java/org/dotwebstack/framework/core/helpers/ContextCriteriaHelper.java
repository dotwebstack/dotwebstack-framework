package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isQuery;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.execution.ExecutionStepInfo;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.model.Subscription;
import org.dotwebstack.framework.core.query.model.ContextCriteria;

public class ContextCriteriaHelper {

  private ContextCriteriaHelper() {}

  public static Optional<String> getContextName(Schema schema, ExecutionStepInfo requestStepInfo) {
    var parentType = requestStepInfo.getParent()
        .getType();
    var selectionName = requestStepInfo.getFieldDefinition()
        .getName();

    if (isQuery(parentType)) {
      return Optional.of(schema.getQueries()
          .get(selectionName))
          .map(Query::getContext);
    }

    if (isSubscription(parentType)) {
      return Optional.of(schema.getSubscriptions()
          .get(selectionName))
          .map(Subscription::getContext);
    }

    throw illegalArgumentException(
        "The parent type of the given requestStepInfo is not of type 'Query' or 'Subscription");
  }

  public static ContextCriteria createContextCriteria(Schema schema, ExecutionStepInfo requestStepInfo) {
    return getContextName(schema, requestStepInfo).map(name -> {
      var context = schema.getContexts()
          .get(name);

      var builder = ContextCriteria.builder()
          .name(name)
          .context(context);

      Map<String, Object> arguments = getNestedMap(requestStepInfo.getArguments(), CONTEXT_ARGUMENT_NAME);

      builder.values(arguments);

      return builder.build();
    })
        .orElse(null);
  }
}
