package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.graphql.GraphqlObjectShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.graphql.Rdf4jGraphqlConfigurer;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.graphql.GraphqlConfiguration;
import org.dotwebstack.framework.graphql.scalars.ScalarConfigurer;
import org.dotwebstack.framework.test.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {BackendConfiguration.class, GraphqlConfiguration.class})
@Import({LocalBackendConfigurer.class, ScalarConfigurer.class, Rdf4jGraphqlConfigurer.class,
    GraphqlObjectShapeRegistry.class, ShaclDirectiveWiring.class, SparqlDirectiveWiring.class})
class ShaclDirectiveWiringTest {

  @Autowired
  private BackendRegistry backendRegistry;

  @Autowired
  private GraphQLSchema schema;

  private GraphqlObjectShapeRegistry objectShapeRegistry;

  private ShaclDirectiveWiring shaclDirectiveWiring;

  @BeforeEach
  void setUp() {
    objectShapeRegistry = new GraphqlObjectShapeRegistry();
    shaclDirectiveWiring = new ShaclDirectiveWiring(backendRegistry, objectShapeRegistry);
  }

  @Test
  void onObject_RegistersNodeShapes_ForAnnotatedTypes() {
    // Arrange
    GraphQLObjectType objectType = schema.getObjectType(Constants.BUILDING_TYPE);
    GraphQLDirective directive = objectType.getDirective(Directives.SHACL_NAME);
    SchemaDirectiveWiringEnvironment<GraphQLObjectType> environment = createEnvironment(objectType,
        directive);

    // Act
    GraphQLObjectType result = shaclDirectiveWiring.onObject(environment);

    // Assert
    assertThat(result, is(equalTo(objectType)));
    NodeShape nodeShape = objectShapeRegistry.get(objectType);
    assertThat(nodeShape.getIdentifier(), is(equalTo(Constants.BUILDING_SHAPE)));
  }

  private static SchemaDirectiveWiringEnvironment<GraphQLObjectType> createEnvironment(
      GraphQLObjectType objectType, GraphQLDirective directive) {
    @SuppressWarnings("unchecked")
    SchemaDirectiveWiringEnvironment<GraphQLObjectType> environment =
        mock(SchemaDirectiveWiringEnvironment.class);

    when(environment.getElement()).thenReturn(objectType);
    when(environment.getDirective()).thenReturn(directive);

    return environment;
  }

}
