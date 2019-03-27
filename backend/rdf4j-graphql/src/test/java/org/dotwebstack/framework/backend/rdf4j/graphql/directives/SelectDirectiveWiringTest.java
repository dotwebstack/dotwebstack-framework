package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import static org.dotwebstack.framework.test.Constants.BUILDINGS_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_REQ_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.graphql.Rdf4jGraphqlConfigurer;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectListFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectOneFetcher;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.graphql.GraphqlConfiguration;
import org.dotwebstack.framework.graphql.scalars.ScalarConfigurer;
import org.dotwebstack.framework.test.Constants;
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
  @ValueSource(strings = {BUILDING_FIELD, BUILDING_REQ_FIELD})
  void onField_registersSelectOneFetcher_forObjectType(String fieldName) {
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
            .getDataFetcher(objectType, objectType.getFieldDefinition(BUILDING_IDENTIFIER_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(BUILDING_HEIGHT_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
  }

  @Test
  void onField_throwsException_forListType() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType()
        .getFieldDefinition(Constants.BUILDINGS_FIELD);
    GraphQLCodeRegistry codeRegistry = schema.getCodeRegistry();
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), codeRegistry);

    // Act
    GraphQLFieldDefinition result = selectDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(equalTo(fieldDefinition)));

    DataFetcher dataFetcher = codeRegistry.getDataFetcher(schema.getQueryType(), fieldDefinition);
    assertThat(dataFetcher, is(instanceOf(SelectListFetcher.class)));

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil
        .unwrapAll(fieldDefinition.getType());
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(BUILDING_IDENTIFIER_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(BUILDING_HEIGHT_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
  }

  @Test
  void onField_throwsException_forScalarListType() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType()
        .getFieldDefinition(BUILDINGS_FIELD)
        .transform(builder -> builder.type(
            GraphQLList.list(Scalars.GraphQLString)));
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), schema.getCodeRegistry());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        selectDirectiveWiring.onField(environment));
  }


  @Test
  void onField_throwsException_forScalarType() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType()
        .getFieldDefinition(BUILDING_FIELD)
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
    GraphQLObjectType outputType = schema.getObjectType(BUILDING_TYPE)
        .transform(GraphQLObjectType.Builder::clearDirectives);
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType()
        .getFieldDefinition(BUILDING_FIELD)
        .transform(builder -> builder.type(outputType));
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition,
        fieldDefinition.getDirective(Directives.SELECT_NAME), schema.getCodeRegistry());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        selectDirectiveWiring.onField(environment));
  }

  @Test
  void onField_throwsException_forAbsentBackend() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = schema.getQueryType()
        .getFieldDefinition(BUILDING_FIELD);
    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name(Directives.SELECT_NAME)
        .argument(GraphQLArgument.newArgument()
            .name(Directives.SELECT_ARG_BACKEND)
            .type(Scalars.GraphQLString)
            .value("foo"))
        .argument(GraphQLArgument.newArgument()
            .name(Directives.SELECT_ARG_SUBJECT)
            .type(Scalars.GraphQLString)
            .value("bar"))
        .build();
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = createEnvironment(
        schema.getQueryType(), fieldDefinition, directive, schema.getCodeRegistry());

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
