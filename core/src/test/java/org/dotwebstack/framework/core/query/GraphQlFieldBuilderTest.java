package org.dotwebstack.framework.core.query;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import graphql.language.FieldDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.junit.jupiter.api.Test;

class GraphQlFieldBuilderTest {

  private final TypeDefinitionRegistry registry = mock(TypeDefinitionRegistry.class);

  @Test
  void toGraphQlField_throwsException_MissingType() {
    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    FieldDefinition fieldDefinition = createFieldDefinition();

    Map<String, GraphQlField> typeNameFieldMap = new HashMap<>();
    assertThrows(InvalidConfigurationException.class, () -> builder.toGraphQlField(fieldDefinition, typeNameFieldMap));
  }

  private FieldDefinition createFieldDefinition() {
    return FieldDefinition.newFieldDefinition()
        .name("founded")
        .type(TypeUtils.newNonNullableType("DateTime"))
        .build();
  }
}
