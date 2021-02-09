package org.dotwebstack.framework.backend.postgres.query;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  private static final String FIELD_BREWERY = "brewery";

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  private QueryBuilder queryBuilder;

  @BeforeEach
  void beforeAll() {
    DSLContext dslContext = createDslContext();

    queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext);
  }

  @Test
  void buildwithKeyCondition() {
    // Arrange
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    ColumnKeyCondition keyCondition = ColumnKeyCondition.builder()
        .valueMap(Map.of("identifier", "id-1"))
        .build();

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(FIELD_IDENTIFIER, FIELD_NAME);

    // Act
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, keyCondition, selectionSet);

    // Assert
    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select x3, t3.* from (values (:1)) as \"t2\" (\"x3\") join lateral (select \"t1\".\"identifier\" "
            + "as \"x1\", \"t1\".\"name\" as \"x2\" from db.beer as \"t1\" where identifier "
            + "= \"t2\".\"x3\" limit :2) as \"t3\" on true"));
  }

  @Test
  void buildWithoutKeyCondition() {
    // Arrange
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(FIELD_IDENTIFIER, FIELD_NAME);

    // Act
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, (KeyCondition) null, selectionSet);

    // Assert
    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select \"t1\".\"identifier\" as \"x1\", \"t1\".\"name\" as \"x2\" from db.beer as \"t1\" limit :1"));
  }

  @Test
  void buildWithJoinColumn() {
    // Arrange
    GraphQLObjectType breweryType = GraphQLObjectType.newObject()
        .name("Brewery")
        .build();

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_NAME, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_BREWERY, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_BREWERY)
            .type(breweryType)
            .build()));

    when(selectionSet.getFields("*.*")).thenReturn(selectedFields);

    selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER, GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_IDENTIFIER)
        .type(Scalars.GraphQLString)
        .build()), mockSelectedField(FIELD_NAME,
            GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME)
                .type(Scalars.GraphQLString)
                .build()));

    when(selectionSet.getFields("brewery/*.*")).thenReturn(selectedFields);


    when(dotWebStackConfiguration.getTypeConfiguration(breweryType.getName()))
        .thenReturn(createBreweryTypeConfiguration());

    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    // Act
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, (KeyCondition) null, selectionSet);

    // Assert
    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select \"t1\".\"identifier\" as \"x1\", \"t1\".\"name\" as \"x2\", t3.* from db.beer as \"t1\" "
            + "left outer join lateral (select \"t2\".\"identifier\" as \"x3\", \"t2\".\"name\" as \"x4\" "
            + "from db.brewery as \"t2\" where \"t1\".\"brewery\" = \"identifier\" limit :1) as \"t3\" "
            + "on true limit :2"));
  }

  @Test
  void buildWithJoinTable() {
    // Arrange
    PostgresTypeConfiguration typeConfiguration = createIngredientTypeConfiguration();

    ColumnKeyCondition keyCondition = ColumnKeyCondition.builder()
        .valueMap(Map.of("beers_identifier", "id-1"))
        .joinTable(
            new JoinTable("dbeerpedia.beers_ingredients", List.of(new JoinColumn("beers_identifier", "identifier")),
                List.of(new JoinColumn("ingredients_identifier", "identifier"))))
        .build();

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_NAME, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME)
            .type(Scalars.GraphQLString)
            .build()));

    when(selectionSet.getFields("*.*")).thenReturn(selectedFields);

    // Act
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, keyCondition, selectionSet);

    // Assert
    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select x3, t4.* from (values (:1)) as \"t3\" (\"x3\") join lateral (select \"t1\".\"identifier\" "
            + "as \"x1\", \"t1\".\"name\" as \"x2\" from dbeerpedia.ingredients as \"t1\" "
            + "join dbeerpedia.beers_ingredients as \"t2\" on \"t2\".\"ingredients_identifier\" "
            + "= \"t1\".\"identifier\" where beers_identifier = \"t3\".\"x3\" limit :2) as \"t4\" on true"));
  }

  private SelectedField mockSelectedField(String name) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    return selectedField;
  }

  private SelectedField mockSelectedField(String name, GraphQLFieldDefinition fieldDefinition) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    lenient().when(selectedField.getFieldDefinition())
        .thenReturn(fieldDefinition);
    lenient().when(selectedField.getFullyQualifiedName())
        .thenReturn(name);
    return selectedField;
  }

  private DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet(String... selectedFields) {
    return mockDataFetchingFieldSelectionSet(Arrays.stream(selectedFields)
        .map(this::mockSelectedField)
        .collect(Collectors.toList()));
  }

  private DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet(List<SelectedField> selectedFields) {
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    when(selectionSet.getFields(any())).thenReturn(selectedFields);

    return selectionSet;
  }

  private PostgresTypeConfiguration createIngredientTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));
    typeConfiguration.setFields(new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration())));
    typeConfiguration.setTable("dbeerpedia.ingredients");

    typeConfiguration.init(newObjectTypeDefinition().name("Ingredient")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .build())
        .build());

    return typeConfiguration;
  }

  private PostgresTypeConfiguration createBreweryTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));
    typeConfiguration.setFields(new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration())));
    typeConfiguration.setTable("db.brewery");

    typeConfiguration.init(newObjectTypeDefinition().name("Brewery")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .build())
        .build());

    return typeConfiguration;
  }

  private PostgresTypeConfiguration createBeerTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));

    PostgresFieldConfiguration breweryFieldConfiguration = new PostgresFieldConfiguration();
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName("brewery");
    joinColumn.setReferencedField(FIELD_IDENTIFIER);
    breweryFieldConfiguration.setJoinColumns(List.of(joinColumn));

    typeConfiguration.setFields(new HashMap<>(
        Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration(), FIELD_BREWERY, breweryFieldConfiguration)));

    typeConfiguration.setTable("db.beer");

    typeConfiguration.init(newObjectTypeDefinition().name("Beer")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_BREWERY)
            .build())
        .build());

    return typeConfiguration;
  }

  private DSLContext createDslContext() {
    MockConnection connection = new MockConnection(new TestDataProvider());

    return DSL.using(connection, SQLDialect.POSTGRES);
  }

  private static class TestDataProvider implements MockDataProvider {

    @Override
    public MockResult[] execute(MockExecuteContext mockExecuteContext) {
      return null;
    }
  }

}
