package org.dotwebstack.framework.service.openapi.validation;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.ExecutionInput;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
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
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.query.OasToGraphQlHelper;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasObjectField;
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
      validateQuery(queryType, methodOperation, schemaContext, typeDefinitionRegistry, validationResult);
    });
    return validationResult;
  }

  private void validateQuery(ObjectTypeDefinition queryType, HttpMethodOperation methodOperation,
      ResponseSchemaContext schemaContext, TypeDefinitionRegistry typeDefinitionRegistry,
      ValidationResult validationResult) {
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
      Type<?> resultType = fieldDefOptional.get()
          .getType();
      validateResultType(resultType, schemaContext, typeDefinitionRegistry, validationResult);
    }
  }

  private void validateResultType(Type<?> resultType, ResponseSchemaContext schemaContext,
      TypeDefinitionRegistry typeDefinitionRegistry, ValidationResult validationResult) {
    OasField oasRoot = OasToGraphQlHelper.findRootField(schemaContext.getResponses()
        .get(0));
    if (oasRoot != null) {
      TypeDefinition<?> type = resolveTypeDefinition(resultType, typeDefinitionRegistry); // TODO: unwraplater
      type = unwrapConnection(type, typeDefinitionRegistry);
      LOG.debug("Validating graphql result type {} against oas type {}", resultType, oasRoot); // TODO: log field name
      validateType(oasRoot, type, typeDefinitionRegistry, validationResult);
    }
  }

  private void validateType(OasField oasField, TypeDefinition<?> type, TypeDefinitionRegistry typeDefinitionRegistry,
      ValidationResult validationResult) {
    switch (oasField.getType()) {
      case SCALAR:
        break;
      case ARRAY:
        break;
      case OBJECT:
        validateObject((OasObjectField) oasField, type, typeDefinitionRegistry, validationResult);
        break;
      case ONE_OF:
        break;
      case SCALAR_EXPRESSION:
        break;
      default:
        break;
    }
  }

  private void validateObject(OasObjectField oasField, TypeDefinition<?> type,
      TypeDefinitionRegistry typeDefinitionRegistry, ValidationResult validationResult) {
    if (oasField.isEnvelope()) {
      // TODO: unwrap and validate type
    } else {
      if (!(type instanceof ObjectTypeDefinition)) {
        validationResult.addQueryError("bla", "bla", "bla");
      }
      var fields = oasField.getFields();
      var objecTypeDef = (ObjectTypeDefinition) type;
      fields.forEach((name, child) -> {
        // TODO: unwrap child
        var graphqQlOptional = objecTypeDef.getFieldDefinitions()
            .stream()
            .filter(f -> f.getName()
                .equals(name))
            .findFirst();
        if (graphqQlOptional.isEmpty()) {
          validationResult.addQueryError("bla", "bla", "bla");
        } else {
          var childTypeDef = resolveTypeDefinition(graphqQlOptional.get()
              .getType(), typeDefinitionRegistry);
          validateType(child, childTypeDef, typeDefinitionRegistry, validationResult);
        }
      });

    }
  }

  private TypeDefinition<?> unwrapConnection(TypeDefinition<?> typeDefinitionRegistry,
      TypeDefinitionRegistry registry) {
    if (!(typeDefinitionRegistry instanceof ObjectTypeDefinition)) {
      return typeDefinitionRegistry;
    } else {
      ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition) typeDefinitionRegistry;
      var nodeOptional = objectTypeDefinition.getFieldDefinitions()
          .stream()
          .filter(fd -> fd.getName()
              .equals("nodes"))
          .findFirst();
      if (nodeOptional.isPresent()) {
        Type<?> nodesType = nodeOptional.get()
            .getType();
        return resolveTypeDefinition(nodesType, registry);
      }
      return null; // TODO: error
    }
  }

  private TypeDefinition<?> resolveTypeDefinition(Type<?> type, TypeDefinitionRegistry registry) {
    var typeName = (TypeName) TypeHelper.unwrapType(type);
    return registry.getType(typeName.getName())
        .orElseThrow(
            () -> invalidConfigurationException("Type {} not present " + "in typedefinition registry", typeName));
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
