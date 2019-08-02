package org.dotwebstack.framework.service.openapi.response;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLByte;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLShort;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;

public class ResponseContextValidator {

  public void validate(@NonNull ResponseContext responseContext, String pathName) {
    GraphQlField field = responseContext.getGraphQlField();
    long matched = responseContext.getResponses()
        .stream()
        .filter(responseTemplate -> responseTemplate.isApplicable(200, 299))
        .count();
    if (matched == 0) {
      throw ExceptionHelper.unsupportedOperationException("No response in the 200 range found.");
    }
    validateParameters(field, responseContext.getParameters(), pathName);
    responseContext.getResponses()
        .forEach(response -> validate(response.getResponseObject(), field));
  }

  private void validate(ResponseObject template, GraphQlField field) {
    field.getArguments();
    String graphQlType = field.getType();
    String oasType = template.getType();
    switch (oasType) {
      case "array":
        ResponseObject fieldTemplate = template.getItems()
            .get(0);
        validate(fieldTemplate, field);
        break;
      case "object":
        List<ResponseObject> children = template.getChildren();
        children.forEach(child -> {
          GraphQlField graphQlChildField = field.getFields()
              .stream()
              .filter(childField -> childField.getName()
                  .equals(child.getIdentifier()))
              .findFirst()
              .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
                  "OAS field '{}' not found in matching GraphQl object '{}'.", child.getIdentifier(), field.getName()));
          validate(child, graphQlChildField);
        });
        break;
      default:
        validateTypes(oasType, graphQlType, template.getIdentifier());
    }
  }

  private void validateParameters(GraphQlField field, List<Parameter> parameters, String pathName) {
    parameters.stream()
        .forEach(p -> {
          String name = p.getName();
          long matching = field.getArguments()
              .stream()
              .filter(argument -> argument.getName()
                  .equals(name))
              .count();
          if (matching == 0) {
            throw ExceptionHelper.invalidConfigurationException(
                "OAS argument '{}' for path '{}' was " + "not " + "found on GraphQL field '{}'", name, pathName,
                field.getName());
          }
        });
    field.getArguments()
        .forEach(argument -> verifyRequiredNoDefaultArgument(argument, parameters, pathName));
  }

  private void verifyRequiredNoDefaultArgument(GraphQlArgument argument, List<Parameter> parameters, String pathName) {
    if (argument.isRequired() && !argument.isHasDefault()) {
      long matching = parameters.stream()
          .filter(parameter -> Boolean.TRUE.equals(parameter.getRequired()) && parameter.getName()
              .equals(argument.getName()))
          .count();
      if (matching == 0) {
        throw ExceptionHelper.invalidConfigurationException(
            "No required OAS parameter found for required and no-default GraphQL argument" + " '{}' in path '{}'",
            argument.getName(), pathName);
      }
    }
    if (argument.isRequired()) {
      argument.getChildren()
          .forEach(child -> verifyRequiredNoDefaultArgument(child, parameters, pathName));
    }
  }

  protected void validateTypes(String oasType, String graphQlType, String identifier) {
    switch (oasType) {
      case "string":
        break;
      case "number":
        if (!ImmutableList
            .of(GraphQLFloat.getName(), GraphQLInt.getName(), GraphQLLong.getName(), GraphQLByte.getName(),
                GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      case "integer":
        if (!ImmutableList.of(GraphQLInt.getName(), GraphQLByte.getName(), GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      case "boolean":
        if (!GraphQLBoolean.getName()
            .equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      default:
        throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is currently not supported.", oasType);
    }

  }
}
