package org.dotwebstack.framework.core.graphql.directives;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveWiringTest {

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  private final JexlEngine jexlEngine = new JexlBuilder()
      .silent(false)
      .strict(true)
      .create();

  private final TransformDirectiveWiring transformDirectiveWiring = new TransformDirectiveWiring(
      jexlEngine);

  @BeforeEach
  void setUp() {
    when(environment.getElement()).thenReturn(fieldDefinition);
  }

  @Test
  void onField_ThrowsException_ForNonScalarField() {
    // Arrange
    when(fieldDefinition.getType()).thenReturn(GraphQLObjectType.newObject()
        .name("foo")
        .build());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        transformDirectiveWiring.onField(environment));
  }

}
