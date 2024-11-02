package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.HAS_NEXT;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ConnectionDataFetcher implements DataFetcher<Object> {

  private final PagingConfiguration pagingConfiguration;

  public ConnectionDataFetcher(PagingConfiguration pagingConfiguration) {
    this.pagingConfiguration = pagingConfiguration;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    int firstArgumentValue = getFirstArgumentValue(environment);
    int offsetArgumentValue = getOffsetArgumentValue(environment);

    validateArgumentValues(firstArgumentValue, offsetArgumentValue);

    var newData = Mono.just(Map.of(PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME), offsetArgumentValue, PagingConstants.OFFSET_FIELD_NAME,
            PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME), firstArgumentValue, offsetArgumentValue, HAS_NEXT, false))
        .map(map -> {
          if (environment.getSource() != null) {
            map.putAll(environment.getSource());
          }
          return map;
        });

    var data = new HashMap<String, Object>();
    data.put(PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME), offsetArgumentValue);
    data.put(PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME), firstArgumentValue);
    data.put(PagingConstants.OFFSET_FIELD_NAME, offsetArgumentValue);
    data.put(HAS_NEXT, false);

    if (environment.getSource() != null) {
      data.putAll(environment.getSource());
    }

    return data;
  }

  private int getFirstArgumentValue(DataFetchingEnvironment environment) {
    var fieldDefinition = environment.getFieldDefinition();
    var firstArgument = fieldDefinition.getArgument(FIRST_ARGUMENT_NAME);

    return getArgumentValue(environment, firstArgument);
  }

  private int getOffsetArgumentValue(DataFetchingEnvironment environment) {
    var fieldDefinition = environment.getFieldDefinition();
    var offsetArgument = fieldDefinition.getArgument(OFFSET_ARGUMENT_NAME);

    return getArgumentValue(environment, offsetArgument);
  }

  private int getArgumentValue(DataFetchingEnvironment environment, GraphQLArgument graphQlArgument) {
    return (int) Optional.of(graphQlArgument)
        .map(argument -> environment.getArguments()
            .get(argument.getName()))
        .or(() -> Optional.ofNullable(graphQlArgument.getArgumentDefaultValue()
            .getValue()))
        .orElseThrow(() -> illegalStateException("No argument value found for {}.", graphQlArgument.getName()));
  }

  private void validateArgumentValues(int firstArgumentValue, int offsetArgumentValue) {
    if (firstArgumentValue < 0 || offsetArgumentValue < 0) {
      LOG.warn("Paging arguments are negative, this may result in a slow response.\n'first': {}\n'offset':{}", firstArgumentValue, offsetArgumentValue);
    }

    if (pagingConfiguration.getFirstMaxValue() >= 0 && firstArgumentValue > pagingConfiguration.getFirstMaxValue()) {
      throw requestValidationException("Argument 'first' is not allowed to be higher than {}.", pagingConfiguration.getFirstMaxValue());
    }

    if (pagingConfiguration.getFirstMaxValue() >= 0 && firstArgumentValue < 0) {
      throw requestValidationException("Argument 'first' is not allowed to be lower than 0.", pagingConfiguration.getFirstMaxValue());
    }

    if (pagingConfiguration.getOffsetMaxValue() >= 0 && offsetArgumentValue > pagingConfiguration.getOffsetMaxValue()) {
      throw requestValidationException("Argument 'offset' is not allowed to be higher than {}.", pagingConfiguration.getOffsetMaxValue());
    }

    if (pagingConfiguration.getOffsetMaxValue() >= 0 && offsetArgumentValue < 0) {
      throw requestValidationException("Argument 'offset' is not allowed to be lower than 0.", pagingConfiguration.getOffsetMaxValue());
    }
  }
}
