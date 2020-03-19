package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphqlElementParentTree;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LimitDirectiveWiringTest {

  private LimitDirectiveWiring limitDirectiveWiring;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLArgument> environment;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  @Mock
  private GraphQLArgument argument;

  @Mock
  private GraphQLArgument offsetArgument1;

  @Mock
  private GraphQLArgument offsetArgument2;

  @Mock
  private GraphQLDirective mockDirective;

  @Mock
  private GraphqlElementParentTree parentTree;

  @BeforeEach
  void doBefore() {
    limitDirectiveWiring = new LimitDirectiveWiring();
  }

  @Test
  public void onArgument_throwsError_withScalarField() {
    // Arrange
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getType()).thenReturn(GraphQLString);

    // Act & Assert
    assertThrows(InvalidConfigurationException.class, () -> limitDirectiveWiring.onArgument(environment));
  }

  @Test
  public void onArgument_doesNotThrowError_withListField() {
    // Arrange
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getType()).thenReturn(GraphQLList.list(GraphQLString));
    when(environment.getElement()).thenReturn(argument);

    // Act
    assertDoesNotThrow(() -> limitDirectiveWiring.onArgument(environment));
  }

  @Test
  public void onArgument_throwsError_withMultipleLimitArgumentFields() {
    // Arrange
    when(environment.getElementParentTree()).thenReturn(parentTree);
    when(parentTree.getParentInfo()).thenReturn(Optional.empty());
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn("Beer");
    when(fieldDefinition.getType()).thenReturn(GraphQLList.list(GraphQLString));
    when(fieldDefinition.getArguments()).thenReturn(List.of(offsetArgument1, offsetArgument2));
    when(offsetArgument1.getDirective("limit")).thenReturn(mockDirective);
    when(offsetArgument2.getDirective("limit")).thenReturn(mockDirective);

    // Act
    assertThrows(InvalidConfigurationException.class, () -> limitDirectiveWiring.onArgument(environment));
  }

}
