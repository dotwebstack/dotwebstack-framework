package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_REQ_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_SUBJECT;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_TYPE;
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
import org.dotwebstack.framework.backend.rdf4j.Rdf4jConfiguration;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jConfigurer;
import org.dotwebstack.framework.backend.rdf4j.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.SelectOneFetcher;
import org.dotwebstack.framework.core.CoreConfiguration;
import org.dotwebstack.framework.core.CoreConfigurer;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@Import({CoreConfigurer.class, Rdf4jConfigurer.class, SelectDirectiveWiring.class})
@ContextConfiguration(classes = {CoreConfiguration.class, Rdf4jConfiguration.class})
class SelectDirectiveWiringTest {

  @Autowired
  private RepositoryConnection repositoryConnection;

  @Autowired
  private GraphQLSchema schema;

  private SelectDirectiveWiring selectDirectiveWiring;

  @BeforeEach
  void setUp() {
    selectDirectiveWiring = new SelectDirectiveWiring(repositoryConnection);
  }

  @ParameterizedTest
  @ValueSource(strings = {BUILDING_FIELD, BUILDING_REQ_FIELD})
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
    assertThat(((SelectOneFetcher) dataFetcher).getNodeShape().getIdentifier(),
        is(equalTo(BUILDING_SHAPE)));
    assertThat(((SelectOneFetcher) dataFetcher).getRepositoryConnection(),
        is(equalTo(repositoryConnection)));
    assertThat(((SelectOneFetcher) dataFetcher).getSubjectTemplate(),
        is(equalTo(BUILDING_SUBJECT)));

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil
        .unwrapNonNull(fieldDefinition.getType());
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(BUILDING_IDENTIFIER_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
    assertThat(codeRegistry
            .getDataFetcher(objectType, objectType.getFieldDefinition(BUILDING_HEIGHT_FIELD)),
        is(instanceOf(BindingSetFetcher.class)));
  }

  @ParameterizedTest
  @ValueSource(strings = {BUILDING_FIELD, BUILDING_REQ_FIELD})
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
  @ValueSource(strings = {BUILDING_FIELD, BUILDING_REQ_FIELD})
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
