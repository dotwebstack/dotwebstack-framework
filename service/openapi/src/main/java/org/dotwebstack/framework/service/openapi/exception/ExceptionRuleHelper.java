package org.dotwebstack.framework.service.openapi.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.RequestValidationException;
import org.dotwebstack.framework.core.templating.TemplatingException;
import org.dotwebstack.framework.service.openapi.mapping.MappingException;
import org.dotwebstack.graphql.orchestrate.exception.GraphqlJavaOrchestrateException;

public class ExceptionRuleHelper {

  private ExceptionRuleHelper() {}

  private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";

  private static final String ERROR_WHILE_PROCESSING_REQUEST_MESSAGE = "Error while processing the request";

  public static final List<ExceptionRule> MAPPING = List.of(ExceptionRule.builder()
      .exception(NotAcceptableException.class)
      .responseStatus(NOT_ACCEPTABLE)
      .title("Unsupported media type requested")
      .build(),
      ExceptionRule.builder()
          .exception(ParameterValidationException.class)
          .responseStatus(BAD_REQUEST)
          .title("Error while obtaining request parameters")
          .detail(true)
          .build(),
      ExceptionRule.builder()
          .exception(MappingException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title(INTERNAL_SERVER_ERROR_MESSAGE)
          .build(),
      ExceptionRule.builder()
          .exception(GraphQlErrorException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title(INTERNAL_SERVER_ERROR_MESSAGE)
          .build(),
      ExceptionRule.builder()
          .exception(NoContentException.class)
          .responseStatus(NO_CONTENT)
          .title("No content")
          .build(),
      ExceptionRule.builder()
          .exception(NotFoundException.class)
          .responseStatus(NOT_FOUND)
          .title("No results found")
          .build(),
      ExceptionRule.builder()
          .exception(UnsupportedOperationException.class)
          .responseStatus(UNSUPPORTED_MEDIA_TYPE)
          .title("Not supported")
          .build(),
      ExceptionRule.builder()
          .exception(BadRequestException.class)
          .responseStatus(BAD_REQUEST)
          .title(ERROR_WHILE_PROCESSING_REQUEST_MESSAGE)
          .detail(true)
          .build(),
      ExceptionRule.builder()
          .exception(RequestValidationException.class)
          .responseStatus(BAD_REQUEST)
          .title(ERROR_WHILE_PROCESSING_REQUEST_MESSAGE)
          .detail(true)
          .build(),
      ExceptionRule.builder()
          .exception(InvalidConfigurationException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title("Bad configuration")
          .build(),
      ExceptionRule.builder()
          .exception(TemplatingException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title("Templating went wrong")
          .build(),
      ExceptionRule.builder()
          .exception(GraphqlJavaOrchestrateException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title(INTERNAL_SERVER_ERROR_MESSAGE)
          .build(),
      ExceptionRule.builder()
          .exception(GraphqlJavaOrchestrateException.class)
          .responseStatus(BAD_REQUEST)
          .title(ERROR_WHILE_PROCESSING_REQUEST_MESSAGE)
          .detail(true)
          .build());

  public static Optional<ExceptionRule> getExceptionRule(Throwable throwable) {
    var matchingRules = MAPPING.stream()
        .filter(rule -> rule.getException()
            .isAssignableFrom(throwable.getClass()))
        .toList();

    /*
     * The dotwebstack graphql-java-orchestrate could return different exceptions of the
     * GraphqlJavaOrchestrateException.class. These are identifiable bij HttpStatus. ExceptionRules of
     * GraphqlJavaOrchestrateException.class can be matched using the HttpStatus.
     */

    if (matchingRules.size() > 1
        && throwable instanceof GraphqlJavaOrchestrateException graphqlJavaOrchestrateException) {
      return matchingRules.stream()
          .filter(matchingRule -> {
            var httpStatus = graphqlJavaOrchestrateException.getStatusCode();
            return matchingRule.getResponseStatus()
                .equals(httpStatus);
          })
          .findFirst();
    } else {
      return matchingRules.stream()
          .findFirst();
    }
  }
}
