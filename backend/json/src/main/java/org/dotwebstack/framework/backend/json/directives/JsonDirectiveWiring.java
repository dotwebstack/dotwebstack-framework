package org.dotwebstack.framework.backend.json.directives;

import static org.dotwebstack.framework.backend.json.query.JsonResourceLoader.loadJsonResource;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.jayway.jsonpath.JsonPath;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.json.query.JsonQueryFetcher;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class JsonDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private final JsonQueryFetcher jsonQueryFetcher;

  public JsonDirectiveWiring(@NonNull JsonQueryFetcher jsonQueryFetcher) {
    this.jsonQueryFetcher = jsonQueryFetcher;
  }

  @Override
  public String getDirectiveName() {
    return JsonDirectives.JSON_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLType outputType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    validateOutputType(outputType);
    validateDirective(environment);

    registerDataFetcher(environment);

    return fieldDefinition;
  }

  private void validateOutputType(GraphQLType outputType) {
    boolean supported = outputType instanceof GraphQLObjectType;
    if (!supported) {
      throw illegalArgumentException("Output type of type %s not supported.", outputType.getClass());
    }
  }

  private void validateDirective(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLDirective directive = environment.getDirective();

    validateDirectiveName(directive);
    validateDirectiveFile(directive);
    validateDirectivePathAndPredicate(environment);
  }

  private void validateDirectiveName(GraphQLDirective graphQlDirective) {
    if (!graphQlDirective.getName()
        .equals(JsonDirectives.JSON_NAME)) {
      throw invalidConfigurationException("Directive %s not supported for json-backend.", graphQlDirective.getName());
    }
  }

  private void validateDirectiveFile(GraphQLDirective graphQlDirective) {
    GraphQLArgument fileArgument = graphQlDirective.getArgument(JsonDirectives.ARGS_FILE);
    if (fileArgument != null) {
      String jsonDataFileName = fileArgument.getValue()
          .toString();
      loadJsonResource(jsonDataFileName);
    }
  }

  private void validateDirectivePathAndPredicate(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLDirective jsonDirective = environment.getDirective();
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    GraphQLArgument pathArgument = jsonDirective.getArgument(JsonDirectives.ARGS_PATH);
    if (pathArgument != null) {
      String jsonPathTemplate = String.valueOf(pathArgument.getValue());

      validateJsonPath(jsonPathTemplate);

      validatePredicates(fieldDefinition, jsonPathTemplate);
    }
  }

  private void validatePredicates(GraphQLFieldDefinition fieldDefinition, String jsonPathTemplate) {
    int expectedNumberOfPredicates = StringUtils.countMatches(jsonPathTemplate, "?");

    long amountOfPredicateDirectives = fieldDefinition.getArguments()
        .stream()
        .filter(argument -> argument.getDirective(PredicateDirectives.PREDICATE_NAME) != null)
        .map(argument -> {
          validateMandatoryArguments(argument);
          return argument;
        })
        .count();

    if (expectedNumberOfPredicates != amountOfPredicateDirectives) {
      throw invalidConfigurationException("Expected %s predicate(s), found: %s.", expectedNumberOfPredicates,
          amountOfPredicateDirectives);
    }
  }

  private void validateMandatoryArguments(GraphQLArgument argument) {
    if (!GraphQLTypeUtil.isNonNull(argument.getType())) {
      throw invalidConfigurationException("Argument: %s with type: '%s' should be mandatory.", argument.getName(),
          argument.getType());
    }
  }

  private void validateJsonPath(String jsonPath) {
    JsonPath.compile(jsonPath.replace("?", "?(@.key == 'value')"));
  }

  private void registerDataFetcher(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLCodeRegistry.Builder codeRegistry = environment.getCodeRegistry();

    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    GraphQLFieldDefinition fieldDefinition = environment.getElement();

    codeRegistry.dataFetcher(fieldsContainer, fieldDefinition, jsonQueryFetcher);
  }
}
