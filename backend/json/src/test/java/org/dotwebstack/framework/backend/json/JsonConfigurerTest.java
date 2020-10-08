package org.dotwebstack.framework.backend.json;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonConfigurerTest {

  private JsonConfigurer jsonConfigurer;

  @Mock
  private TypeDefinitionRegistry typeDefinitionRegistryMock;

  @Test
  void createJsonDefinitionTest() {
    jsonConfigurer = new JsonConfigurer();
    assertDoesNotThrow(() -> jsonConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistryMock));
  }
}
