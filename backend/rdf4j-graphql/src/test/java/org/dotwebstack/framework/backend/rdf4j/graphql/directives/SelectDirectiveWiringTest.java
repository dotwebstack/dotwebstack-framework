package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.graphql.Constants;
import org.dotwebstack.framework.backend.rdf4j.graphql.Rdf4jGraphqlConfigurer;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectOneFetcher;
import org.dotwebstack.framework.backend.rdf4j.local.LocalBackendConfigurer;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.graphql.GraphqlConfiguration;
import org.dotwebstack.framework.graphql.scalars.ScalarConfigurer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {BackendConfiguration.class, GraphqlConfiguration.class})
@Import({LocalBackendConfigurer.class, ScalarConfigurer.class, Rdf4jGraphqlConfigurer.class,
    SelectDirectiveWiring.class})
class SelectDirectiveWiringTest {

  @Autowired
  private BackendRegistry backendRegistry;

  @Autowired
  private GraphQLSchema schema;

  private SelectDirectiveWiring selectDirectiveWiring;

  @BeforeEach
  void setUp() {
    selectDirectiveWiring = new SelectDirectiveWiring(backendRegistry);
  }

  @ParameterizedTest
  @ValueSource(strings = {Constants.BUILDING_FIELD, Constants.BUILDING_REQ_FIELD})
  void onField_registersSelectOneFetcher_forObjectFields(String fieldName) {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType().getFieldDefinition(fieldName);
    GraphQLCodeRegistry codeRegistry = schema.getCodeRegistry();
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), codeRegistry);

    // Act
    GraphQLFieldDefinition result = selectDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(equalTo(fieldDefinition)));

    DataFetcher dataFetcher = codeRegistry.getDataFetcher(schema.getQueryType(), fieldDefinition);
    assertThat(dataFetcher, is(instanceOf(SelectOneFetcher.class)));

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil
        .unwrapNonNull(fieldDefinition.getType());
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(
                Constants.BUILDING_IDENTIFIER_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(
                Constants.BUILDING_HEIGHT_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
  }

  @ParameterizedTest
  @ValueSource(strings = {Constants.BUILDING_FIELD, Constants.BUILDING_REQ_FIELD})
  void onField_throwsException_forListOutputType(String fieldName) {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType().getFieldDefinition(fieldName)
        .transform(builder -> builder
            .type(GraphQLList.list(schema.getQueryType().getFieldDefinition(fieldName))));
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), schema.getCodeRegistry());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        selectDirectiveWiring.onField(environment));
  }

  @ParameterizedTest
  @ValueSource(strings = {Constants.BUILDING_FIELD, Constants.BUILDING_REQ_FIELD})
  void onField_throwsException_forScalarOutputType(String fieldName) {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType().getFieldDefinition(fieldName)
        .transform(builder -> builder.type(Scalars.GraphQLString));
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), schema.getCodeRegistry());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        selectDirectiveWiring.onField(environment));
  }

  @Test
  void onField_throwsException_forMissingSubjectDirective() {
    // Arrange
    GraphQLObjectType outputType = schema.getObjectType(Constants.BUILDING_TYPE)
        .transform(GraphQLObjectType.Builder::clearDirectives);
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType()
        .getFieldDefinition(Constants.BUILDING_FIELD)
        .transform(builder -> builder.type(outputType));
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), schema.getCodeRegistry());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        selectDirectiveWiring.onField(environment));
  }

  private static SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> createEnvironment(
      GraphQLFieldsContainer parentType, GraphQLFieldDefinition fieldDefinition,
      GraphQLDirective directive, GraphQLCodeRegistry codeRegistry) {
    @SuppressWarnings("unchecked")
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment =
        mock(SchemaDirectiveWiringEnvironment.class);

    when(environment.getFieldsContainer()).thenReturn(parentType);
    when(environment.getElement()).thenReturn(fieldDefinition);
    when(environment.getDirective()).thenReturn(directive);
    when(environment.getCodeRegistry())
        .thenReturn(GraphQLCodeRegistry.newCodeRegistry(codeRegistry));

    return environment;
  }

}
