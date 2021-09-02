package org.dotwebstack.framework.service.openapi.validation;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.ExecutionInput;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "dotwebstack.openapi.oasGraphQlValidation", havingValue = "true")
public class OasGraphQlValidator {

  private final GraphQlService graphQlService;

  private final Map<HttpMethodOperation, ResponseSchemaContext> operationResponseMap;

  public OasGraphQlValidator(GraphQlService graphQlService,
      Map<HttpMethodOperation, ResponseSchemaContext> operationResponseMap) {
    this.graphQlService = graphQlService;
    this.operationResponseMap = operationResponseMap;
  }

  @PostConstruct
  public void validate() {
    LOG.debug("Performing validation against graphql schema");
    ValidationResult result;
    try {
      var typeDefinitionRegistry = fetchTypeDefregistry();
      result = validate(typeDefinitionRegistry);
    } catch (Exception e) {
      throw invalidConfigurationException("Could not perform validation against graphQl Schema", e);
    }
    if (result.hasErrors()) {
      throw invalidConfigurationException("Validation of openApi schema agains graphQl schema resulted in errors: {}",
          result.toString());
    }
  }

  private ValidationResult validate(TypeDefinitionRegistry typeDefinitionRegistry) {
    LOG.debug("validating...");
    var validationResult = new ValidationResult();
    var queryTypeOptional = typeDefinitionRegistry.getType("Query");
    if (queryTypeOptional.isEmpty()) {
      validationResult.getGlobalErrors()
          .add("Could not find 'Query' type in graphql schema");
      return validationResult;
    }
    var queryType = (ObjectTypeDefinition) queryTypeOptional.get();

    this.operationResponseMap.forEach((methodOperation, schemaContext) -> {
      LOG.debug("Validating {} operation of query {}", methodOperation.getHttpMethod()
          .name(), methodOperation.getName());
      var queryName = schemaContext.getDwsQuerySettings()
          .getQueryName();
      var fieldDefOptional = queryType.getFieldDefinitions()
          .stream()
          .filter(fd -> fd.getName()
              .equals(queryName))
          .findFirst();
      if (fieldDefOptional.isEmpty()) {
        validationResult.addQueryError(queryName, methodOperation.getName(), "Could not find query in graphql schema");
      } else {
        LOG.debug("Query valid");
      }
    });
    return validationResult;
  }

  private TypeDefinitionRegistry fetchTypeDefregistry() throws IOException {
    var input = ExecutionInput.newExecutionInput()
        .query(loadQuery())
        .build();
    var executionResult = this.graphQlService.execute(input);

    if (executionResult.getErrors() != null && !executionResult.getErrors()
        .isEmpty()) {
      throw invalidConfigurationException("Graphql introspection query returned errors: [{}]",
          executionResult.getErrors());
    }
    Map<String, Object> data = executionResult.getData();
    var schemaDoc = new IntrospectionResultToSchema().createSchemaDefinition(data);

    return new SchemaParser().buildRegistry(schemaDoc);
  }

  private String loadQuery() throws IOException {
    try (InputStream inputStream = OasGraphQlValidator.class.getClassLoader()
        .getResourceAsStream("graphql/introspection_query.txt")) {
      if (inputStream == null) {
        throw new IOException("Could not get inputstream for graphql introspection query");
      }
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
  }

}
