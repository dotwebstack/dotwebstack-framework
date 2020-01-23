package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.language.Directive;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.SchemaValidator;
import org.dotwebstack.framework.core.SchemaValidatorException;
import org.springframework.stereotype.Component;

@Component
public class SchemaModelValidator implements SchemaValidator {

  private static final String QUERY_TYPE_NAME = "Query";

  private static final String SPARQL_DIRECTIVE_NAME = "sparql";

  private static final String MODEL_TYPE_NAME = "Model";

  private TypeDefinitionRegistry typeDefinitionRegistry;

  public SchemaModelValidator(@NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  @Override
  public void validate() {
    typeDefinitionRegistry.getType(QUERY_TYPE_NAME)
        .ifPresent(this::validateQuery);
  }

  private void validateQuery(TypeDefinition typeDefinition) {
    if (typeDefinition instanceof ObjectTypeDefinition) {
      getModelOutputTypes((ObjectTypeDefinition) typeDefinition).stream()
          .forEach(this::validateSparqlQueryDirectivePresent);
    }
  }

  private List<FieldDefinition> getModelOutputTypes(ObjectTypeDefinition typeDefinition) {
    return typeDefinition.getFieldDefinitions()
        .stream()
        .filter(this::isModelType)
        .collect(Collectors.toList());
  }

  private boolean isModelType(FieldDefinition fieldDefinition) {
    if (fieldDefinition != null && fieldDefinition.getType() instanceof TypeName) {
      return MODEL_TYPE_NAME.equals(((TypeName) fieldDefinition.getType()).getName());
    }
    return false;
  }

  private void validateSparqlQueryDirectivePresent(FieldDefinition fieldDefinition) {
    if (hasNoSparqlDirective(fieldDefinition.getDirectives())) {
      throw new SchemaValidatorException(
          "For the field definition '{}' the 'sparql' directive is mandatory for type Model.",
          fieldDefinition.getName());
    }
  }

  private boolean hasNoSparqlDirective(List<Directive> directives) {
    return directives.stream()
        .noneMatch(directive -> SPARQL_DIRECTIVE_NAME.equals(directive.getName()));
  }
}
