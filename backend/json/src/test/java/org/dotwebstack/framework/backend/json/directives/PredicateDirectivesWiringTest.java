package org.dotwebstack.framework.backend.json.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import graphql.schema.GraphQLArgument;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PredicateDirectivesWiringTest {

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLArgument> environmentMock;

  private PredicateDirectiveWiring predicateDirectiveWiring;

  @BeforeEach
  void setup() {
    predicateDirectiveWiring = new PredicateDirectiveWiring();
  }

  @Test
  void getDirectiveNameTest() {
    // Act
    String directiveName = predicateDirectiveWiring.getDirectiveName();

    // Assert
    assertThat(directiveName, equalTo(PredicateDirectives.PREDICATE_NAME));
  }

  @Test
  void onFieldSuccessTest() {
    // Act
    assertDoesNotThrow(() -> predicateDirectiveWiring.onArgument(environmentMock));
  }
}
