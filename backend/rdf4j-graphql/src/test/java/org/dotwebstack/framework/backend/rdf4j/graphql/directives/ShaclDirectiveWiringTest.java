package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.graphql.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.ValueFetcher;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.graphql.scalars.Scalars;
import org.dotwebstack.framework.test.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {BackendConfiguration.class})
@Import({LocalBackendConfigurer.class})
class ShaclDirectiveWiringTest {

  @Autowired
  private BackendRegistry backendRegistry;

  @Value("${dotwebstack.rdf4j.shapeGraph}")
  private String shapeGraph;

  @Mock
  private ValueFetcher valueFetcher;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLObjectType> environment;

  private GraphQLCodeRegistry.Builder codeRegistryBuilder;

  private NodeShapeRegistry nodeShapeRegistry;

  @BeforeEach
  void setUp() {
    codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
    nodeShapeRegistry = new NodeShapeRegistry();
    when(environment.getCodeRegistry()).thenReturn(codeRegistryBuilder);
  }

  @Test
  void onObject_RegistersNodeShapes_ForAnnotatedTypes() {
    // Arrange
    ShaclDirectiveWiring shaclDirectiveWiring =
        new ShaclDirectiveWiring(backendRegistry, nodeShapeRegistry, valueFetcher, shapeGraph);
    shaclDirectiveWiring.initialize();

    GraphQLFieldDefinition identifierField = GraphQLFieldDefinition.newFieldDefinition()
        .name(Constants.BUILDING_IDENTIFIER_FIELD)
        .type(graphql.Scalars.GraphQLID)
        .build();

    GraphQLObjectType objectType = GraphQLObjectType.newObject()
        .name(Constants.BUILDING_TYPE)
        .field(identifierField)
        .build();
    when(environment.getElement()).thenReturn(objectType);

    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name(Directives.SHACL_NAME)
        .argument(GraphQLArgument.newArgument()
            .name(Directives.SHACL_ARG_SHAPE)
            .type(Scalars.IRI)
            .value(Constants.BUILDING_SHAPE))
        .build();
    when(environment.getDirective()).thenReturn(directive);

    // Act
    GraphQLObjectType result = shaclDirectiveWiring.onObject(environment);

    // Assert
    assertThat(result, is(equalTo(objectType)));

    NodeShape nodeShape = nodeShapeRegistry.get(objectType);
    assertThat(nodeShape.getIdentifier(), is(equalTo(Constants.BUILDING_SHAPE)));

    GraphQLCodeRegistry codeRegistry = codeRegistryBuilder.build();
    assertThat(codeRegistry.getDataFetcher(objectType, identifierField),
        instanceOf(ValueFetcher.class));
  }

  @Test
  void onObject_ThrowsException_ForMissingLocalBackend() {
    // Arrange
    BackendRegistry backendRegistry = new BackendRegistry();
    ShaclDirectiveWiring shaclDirectiveWiring =
        new ShaclDirectiveWiring(backendRegistry, nodeShapeRegistry, valueFetcher, shapeGraph);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        shaclDirectiveWiring.initialize());
  }

}
