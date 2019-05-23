package org.dotwebstack.framework.core.directives;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveWiringTest {

  private static final String FIELD_NAME = "foo";

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

  private final TransformDirectiveWiring transformDirectiveWiring = new TransformDirectiveWiring();

  @Test
  void onField_WrapsExistingFetcher_ForScalarFieldWithValue() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(Scalars.GraphQLString)
        .build();
    prepareEnvironment(fieldDefinition);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
  }

  @Test
  void onField_WrapsExistingFetcher_ForNonNullScalarFieldWithValue() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
        .build();
    prepareEnvironment(fieldDefinition);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
  }

  @Test
  void onField_WrapsExistingFetcher_ForListScalarFieldWithValue() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLList.list(Scalars.GraphQLString))
        .build();
    prepareEnvironment(fieldDefinition);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
  }

  @Test
  void onField_WrapsExistingFetcher_ForNonNullListScalarFieldWithValue() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLNonNull.nonNull(GraphQLList.list(
            GraphQLNonNull.nonNull(Scalars.GraphQLString))))
        .build();
    prepareEnvironment(fieldDefinition);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
  }

  @Test
  void onField_ThrowsException_ForNonScalarField() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLObjectType.newObject().name(FIELD_NAME))
        .build();
    when(environment.getElement()).thenReturn(fieldDefinition);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        transformDirectiveWiring.onField(environment));
  }

  private void prepareEnvironment(GraphQLFieldDefinition fieldDefinition) {
    when(environment.getElement()).thenReturn(fieldDefinition);
  }

}
