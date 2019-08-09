package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private String pathName;

  private final ResponseContext responseContext;

  private final ResponseContextValidator responseContextValidator;

  private final GraphQL graphQL;

  private final ObjectMapper objectMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  CoreRequestHandler(String pathName, ResponseContext responseContext,
      ResponseContextValidator responseContextValidator, GraphQL graphQL, ObjectMapper objectMapper,
      ParamHandlerRouter paramHandlerRouter) {
    this.pathName = pathName;
    this.responseContext = responseContext;
    this.graphQL = graphQL;
    this.objectMapper = objectMapper;
    this.paramHandlerRouter = paramHandlerRouter;
    this.responseContextValidator = responseContextValidator;
    validateSchema();
  }

  private void validateSchema() {
    GraphQlField field = responseContext.getGraphQlField();
    if (responseContext.getResponses()
        .stream()
        .noneMatch(responseTemplate -> responseTemplate.isApplicable(200, 299))) {
      throw ExceptionHelper.unsupportedOperationException("No response in the 200 range found.");
    }
    validateParameters(field, responseContext.getParameters(), pathName);
    responseContext.getResponses()
        .forEach(response -> responseContextValidator.validate(response.getResponseObject(), field));
  }

  private void validateParameters(GraphQlField field, List<Parameter> parameters, String pathName) {
    parameters.forEach(parameter -> this.paramHandlerRouter.getParamHandler(parameter)
        .validate(field, parameter, pathName));
    field.getArguments()
        .forEach(argument -> verifyRequiredWithoutDefaultArgument(argument, parameters, pathName));
  }

  private void verifyRequiredWithoutDefaultArgument(GraphQlArgument argument, List<Parameter> parameters,
      String pathName) {
    if (argument.isRequired() && !argument.isHasDefault()) {
      if (parameters.stream()
          .noneMatch(parameter -> Boolean.TRUE.equals(parameter.getRequired()) && parameter.getName()
              .equals(argument.getName()))) {
        throw ExceptionHelper.invalidConfigurationException(
            "No required OAS parameter found for required and no-default GraphQL argument" + " '{}' in path '{}'",
            argument.getName(), pathName);
      }
    }
    if (argument.isRequired()) {
      argument.getChildren()
          .forEach(child -> verifyRequiredWithoutDefaultArgument(child, parameters, pathName));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    try {
      Map<String, Object> inputParams = resolveParameters(request);

      String query = buildQueryString(inputParams);
      ExecutionInput executionInput = ExecutionInput.newExecutionInput()
          .query(query)
          .variables(inputParams)
          .build();

      ExecutionResult result = graphQL.execute(executionInput);
      if (result.getErrors()
          .isEmpty()) {
        ResponseTemplate template = responseContext.getResponses()
            .stream()
            .filter(response -> response.isApplicable(200, 299))
            .findFirst()
            .orElseThrow(
                () -> ExceptionHelper.unsupportedOperationException("No response found within the 200 range."));

        Object response = new ResponseMapper().mapResponse(template.getResponseObject(),
            ((Map<String, Object>) result.getData()).get(this.responseContext.getGraphQlField()
                .getName()));
        if (response == null) {
          return ServerResponse.notFound()
              .build();
        }
        String json = toJson(response);
        return ServerResponse.ok()
            .contentType(MediaType.parseMediaType(template.getMediaType()))
            .body(fromObject(json));
      }
      return ServerResponse.status(HttpStatus.BAD_REQUEST)
          .body(fromObject(String.format("GraphQl query resulted in errors: %s.", toJson(result.getErrors()))));
    } catch (JsonProcessingException e) {
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(fromObject("Error while serializing response to JSON."));
    } catch (ParameterValidationException e) {
      return ServerResponse.status(HttpStatus.BAD_REQUEST)
          .body(fromObject(String.format("Error while obtaining request parameters: %s", e.getMessage())));
    }

  }

  private Map<String, Object> resolveParameters(ServerRequest request) throws ParameterValidationException {
    Map<String, Object> result = new HashMap<>();
    if (Objects.nonNull(this.responseContext.getParameters())) {
      for (Parameter parameter : this.responseContext.getParameters()) {
        paramHandlerRouter.getParamHandler(parameter)
            .getValue(request, parameter)
            .ifPresent(value -> result.put(parameter.getName(), value));
      }
    }
    return result;
  }

  private String toJson(Object object) throws JsonProcessingException {
    return objectMapper.writer()
        .writeValueAsString(object);
  }

  private String buildQueryString(Map<String, Object> inputParams) {
    return new GraphQlQueryBuilder().toQuery(this.responseContext.getGraphQlField(), inputParams);
  }
}
