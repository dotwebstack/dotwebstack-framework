package org.dotwebstack.framework.backend.json;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import graphql.schema.idl.RuntimeWiring;
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

  @Test
  void configureRuntimeWiringShouldNotThrowException() {
    jsonConfigurer = new JsonConfigurer();
    RuntimeWiring.Builder builder = mock(RuntimeWiring.Builder.class);
    assertDoesNotThrow(() -> jsonConfigurer.configureRuntimeWiring(builder));
  }
}
