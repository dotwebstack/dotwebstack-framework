package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.HashMap;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.junit.jupiter.api.Test;

class GraphQlFieldBuilderTest {

  private TypeDefinitionRegistry registry = mock(TypeDefinitionRegistry.class);

  @Test
  void toGraphQlField_throwsException_MissingType() {
    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    FieldDefinition fieldDefinition = createFieldDefinition();

    assertThrows(InvalidConfigurationException.class, () -> builder.toGraphQlField(fieldDefinition, new HashMap<>()));
  }

  private FieldDefinition getQueryFieldDefinition(String name) {
    ObjectTypeDefinition query = (ObjectTypeDefinition) this.registry.getType("Query")
        .orElseThrow(() -> invalidConfigurationException(""));
    return query.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(name))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException(""));
  }

  private FieldDefinition createFieldDefinition() {
    return FieldDefinition.newFieldDefinition()
        .name("founded")
        .type(TypeUtils.newNonNullableType("DateTime"))
        .build();
  }
}
