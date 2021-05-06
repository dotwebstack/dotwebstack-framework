package org.dotwebstack.framework.backend.postgres.query;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.backend.postgres.query.Page.pageWithDefaultSize;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
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
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
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

  private static final String FIELD_AGE = "age";

  private static final String FIELD_BREWERY = "brewery";

  private static final String FIELD_HISTORY = "history";

  private static final String FIELD_INGREDIENTS = "ingredients";

  private static final String FIELD_AGGREGATE = "aggregate";

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  Map<String, AbstractTypeConfiguration<?>> typeMappingMock;

  private QueryBuilder queryBuilder;

  @BeforeEach
  void beforeAll() {
    DSLContext dslContext = createDslContext();

    queryBuilder = new QueryBuilder(
        new SelectWrapperBuilderFactory(dslContext, dotWebStackConfiguration, new AggregateFieldFactory()), dslContext);
  }

  @Test
  void buildwithKeyCondition() {
    postgresTypeConfigurationMock();
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    ColumnKeyCondition keyCondition = ColumnKeyCondition.builder()
        .valueMap(Map.of("identifier", "id-1"))
        .build();

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(FIELD_IDENTIFIER, FIELD_NAME);

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of(keyCondition))
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select x3, t3.* from (values (:1)) as \"t2\" (\"x3\") "
            + "left outer join lateral (select \"t1\".\"identifier\" "
            + "as \"x1\", \"t1\".\"name\" as \"x2\" from db.beer as \"t1\" where \"t1\".\"identifier\" "
            + "= \"t2\".\"x3\" limit :2 offset :3) as \"t3\" on true"));
  }

  @Test
  void buildWithoutKeyCondition() {
    postgresTypeConfigurationMock();
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(FIELD_IDENTIFIER, FIELD_NAME);

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select \"t1\".\"identifier\" as \"x1\", \"t1\".\"name\" as \"x2\" from db.beer as \"t1\" limit :1 "
            + "offset :2"));
  }

  @Test
  void buildWithJoinColumn() {
    postgresTypeConfigurationMock();
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

    postgresTypeConfigurationMock();
    PostgresTypeConfiguration breweryTypeConfiguration = createBreweryTypeConfiguration();

    when(dotWebStackConfiguration.getTypeConfiguration(breweryType.getName())).thenReturn(breweryTypeConfiguration);

    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select t3.*, \"t1\".\"identifier\" as \"x3\", \"t1\".\"name\" as \"x4\" from db.beer as \"t1\" "
            + "left outer join lateral (select \"t2\".\"identifier\" as \"x1\", \"t2\".\"name\" as \"x2\" "
            + "from db.brewery as \"t2\" where \"t1\".\"brewery\" = \"t2\".\"identifier\" limit :1) "
            + "as \"t3\" on true limit :2 offset :3"));
  }

  @Test
  void buildWithJoinTable() {
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

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of(keyCondition))
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select x3, t4.* from (values (:1)) as \"t3\" (\"x3\") "
            + "left outer join lateral (select \"t1\".\"identifier\" "
            + "as \"x1\", \"t1\".\"name\" as \"x2\" from dbeerpedia.ingredients as \"t1\" "
            + "join dbeerpedia.beers_ingredients as \"t2\" on \"t2\".\"ingredients_identifier\" "
            + "= \"t1\".\"identifier\" where \"t2\".\"beers_identifier\" = \"t3\".\"x3\" limit :2 offset :3) "
            + "as \"t4\" on true"));
  }

  @Test
  void build_returnsQueryWithoutJoin_forNestedList() {
    postgresTypeConfigurationMock();
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_INGREDIENTS, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_INGREDIENTS)
            .type(GraphQLList.list(GraphQLObjectType.newObject()
                .name("ingredient")
                .build()))
            .build()));

    when(selectionSet.getFields("*.*")).thenReturn(selectedFields);

    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select \"t1\".\"identifier\" as \"x1\" from db.beer as \"t1\" limit :1 offset :2"));
  }

  @Test
  void mapAssembler_returnsNull_missingKeyAlias() {
    postgresTypeConfigurationMock();
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(FIELD_IDENTIFIER, FIELD_NAME);
    Map<String, Object> data = new HashMap<>();

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getMapAssembler(), notNullValue());
    assertThat(queryHolder.getMapAssembler()
        .apply(data), nullValue());
  }

  @Test
  void mapAssembler_returnsMappedRow_default() {
    postgresTypeConfigurationMock();

    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(FIELD_IDENTIFIER, FIELD_NAME);

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getMapAssembler(), notNullValue());

    Map<String, Object> data = new HashMap<>();
    data.put("x1", "AAA");
    data.put("x2", "Beer 1");

    Map<String, Object> result = queryHolder.getMapAssembler()
        .apply(data);
    assertThat(result.get("name"), is("Beer 1"));
    assertThat(result.get("identifier"), is("AAA"));
  }

  @Test
  void build_returnsCorrectQuery_forAggregate() {
    GraphQLObjectType aggregateType = GraphQLObjectType.newObject()
        .name(AGGREGATE_TYPE)
        .build();

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_AGGREGATE, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_AGGREGATE)
            .type(aggregateType)
            .build()));

    when(selectionSet.getFields("*.*")).thenReturn(selectedFields);

    selectedFields = List.of(
        mockSelectedAggregateField(INT_AVG_FIELD, "totalAvg", GraphQLFieldDefinition.newFieldDefinition()
            .name(INT_AVG_FIELD)
            .type(Scalars.GraphQLInt)
            .build(), "soldPerYear"),
        mockSelectedAggregateField(FLOAT_MIN_FIELD, "minAmount", GraphQLFieldDefinition.newFieldDefinition()
            .name(FLOAT_MIN_FIELD)
            .type(Scalars.GraphQLFloat)
            .build(), "soldPerYear"));

    when(selectionSet.getFields("aggregate/*.*")).thenReturn(selectedFields);

    postgresTypeConfigurationMock();
    PostgresTypeConfiguration beerType = createBeerTypeConfiguration();

    when(dotWebStackConfiguration.getTypeConfiguration("Beer")).thenReturn(beerType);

    PostgresTypeConfiguration typeConfiguration = createBreweryTypeConfiguration();

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select t3.*, \"t1\".\"identifier\" as \"x3\" from db.brewery as \"t1\" left outer join lateral "
            + "(select min(\"t2\".\"sold_per_year\") as \"x1\", cast(avg(\"t2\".\"sold_per_year\") as int) as \"x2\" "
            + "from db.beer as \"t2\" where \"t1\".\"identifier\" = \"t2\".\"brewery\" limit :1) as \"t3\" "
            + "on true limit :2 offset :3"));
  }

  @Test
  void build_returnsCorrectQuery_forAggregateStringJoin() {
    GraphQLObjectType aggregateType = GraphQLObjectType.newObject()
        .name(AGGREGATE_TYPE)
        .build();

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_AGGREGATE, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_AGGREGATE)
            .type(aggregateType)
            .build()));

    when(selectionSet.getFields("*.*")).thenReturn(selectedFields);

    selectedFields = List.of(
        mockSelectedAggregateField(INT_AVG_FIELD, "totalAvg", GraphQLFieldDefinition.newFieldDefinition()
            .name(INT_AVG_FIELD)
            .type(Scalars.GraphQLInt)
            .build(), "soldPerYear"),
        mockSelectedAggregateField(STRING_JOIN_FIELD, "stringJoin", GraphQLFieldDefinition.newFieldDefinition()
            .name(STRING_JOIN_FIELD)
            .type(Scalars.GraphQLFloat)
            .build(), "name"));

    when(selectionSet.getFields("aggregate/*.*")).thenReturn(selectedFields);

    postgresTypeConfigurationMock();
    PostgresTypeConfiguration beerType = createBeerTypeConfiguration();

    when(dotWebStackConfiguration.getTypeConfiguration("Beer")).thenReturn(beerType);

    PostgresTypeConfiguration typeConfiguration = createBreweryTypeConfiguration();

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select t3.*, t5.*, \"t1\".\"identifier\" as \"x3\" from db.brewery as \"t1\" "
            + "left outer join lateral (select string_agg(cast(\"t2\".\"name\" as varchar), ',') "
            + "as \"x1\" from db.beer as \"t2\" where \"t1\".\"identifier\" = \"t2\".\"brewery\" limit :1) "
            + "as \"t3\" on true left outer join lateral (select cast(avg(\"t4\".\"sold_per_year\") as int) as \"x2\" "
            + "from db.beer as \"t4\" where \"t1\".\"identifier\" = \"t4\".\"brewery\" limit :2) as \"t5\" on "
            + "true limit :3 offset :4"));
  }

  @Test
  void build_returnsCorrectQuery_forNestedObject() {
    postgresTypeConfigurationMock();
    GraphQLObjectType historyType = GraphQLObjectType.newObject()
        .name("History")
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
        mockSelectedField(FIELD_HISTORY, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_HISTORY)
            .type(historyType)
            .build()));

    when(selectionSet.getFields("*.*")).thenReturn(selectedFields);

    selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER, GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_IDENTIFIER)
        .type(Scalars.GraphQLString)
        .build()), mockSelectedField(FIELD_AGE,
            GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_AGE)
                .type(Scalars.GraphQLString)
                .build()));

    when(selectionSet.getFields("history/*.*")).thenReturn(selectedFields);

    postgresTypeConfigurationMock();
    PostgresTypeConfiguration historyTypeConfiguration = createHistoryTypeConfiguration();

    when(dotWebStackConfiguration.getTypeConfiguration(historyType.getName())).thenReturn(historyTypeConfiguration);

    PostgresTypeConfiguration typeConfiguration = createBreweryTypeConfiguration();

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(selectionSet)
        .keyConditions(List.of())
        .page(pageWithDefaultSize())
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), notNullValue());
    assertThat(queryHolder.getQuery()
        .getSQL(ParamType.NAMED),
        equalTo("select \"t1\".\"identifier\" as \"x1\", \"t1\".\"age\" as \"x2\", \"t1\".\"identifier\" as \"x3\", "
            + "\"t1\".\"name\" as \"x4\" from db.brewery as \"t1\" limit :1 offset :2"));
  }

  private SelectedField mockSelectedAggregateField(String name, String alias, GraphQLFieldDefinition fieldDefinition,
      String fieldArgument) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    when(selectedField.getAlias()).thenReturn(alias);
    Map<String, Object> arguments = Map.of(FIELD_ARGUMENT, fieldArgument);
    when(selectedField.getArguments()).thenReturn(arguments);
    lenient().when(selectedField.getFieldDefinition())
        .thenReturn(fieldDefinition);
    lenient().when(selectedField.getFullyQualifiedName())
        .thenReturn(name);
    GraphQLObjectType type = GraphQLObjectType.newObject()
        .name(AGGREGATE_TYPE)
        .build();
    when(selectedField.getObjectType()).thenReturn(type);
    return selectedField;
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
    GraphQLObjectType beerType = GraphQLObjectType.newObject()
        .name("Beer")
        .build();
    GraphQLFieldDefinition field = GraphQLFieldDefinition.newFieldDefinition()
        .name("Beer")
        .type(beerType)
        .build();

    GraphQLObjectType objectType = mock(GraphQLObjectType.class);
    lenient().when(selectedField.getObjectType())
        .thenReturn(objectType);
    lenient().when(objectType.getFieldDefinition("beer"))
        .thenReturn(field);
    lenient().when(objectType.getName())
        .thenReturn("Beer");
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

    typeConfiguration.init(Map.of(), newObjectTypeDefinition().name("Ingredient")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .build());

    return typeConfiguration;
  }

  private PostgresTypeConfiguration createHistoryTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));
    typeConfiguration.setFields(new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration())));
    typeConfiguration.setTable("db.brewery");

    typeConfiguration.init(Map.of(), newObjectTypeDefinition().name("History")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_AGE)
            .type(newTypeName(Scalars.GraphQLInt.getName()).build())
            .build())
        .build());

    return typeConfiguration;
  }

  private PostgresTypeConfiguration createBreweryTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();

    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));

    PostgresFieldConfiguration aggregateConfiguration = new PostgresFieldConfiguration();
    aggregateConfiguration.setAggregationOf("Beer");
    aggregateConfiguration.setMappedBy("brewery");

    PostgresFieldConfiguration beerConfiguration = new PostgresFieldConfiguration();
    beerConfiguration.setMappedBy("brewery");
    typeConfiguration.setFields(new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration(), "beer",
        beerConfiguration, FIELD_AGGREGATE, aggregateConfiguration)));
    typeConfiguration.setTable("db.brewery");

    typeConfiguration.init(Map.of("Beer", createBeerTypeConfiguration()), newObjectTypeDefinition().name("Brewery")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_AGGREGATE)
            .type(newTypeName(AGGREGATE_TYPE).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_HISTORY)
            .type(newTypeName("History").build())
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


    JoinTable joinTable = new JoinTable();
    joinTable.setName("beer_ingredients");
    joinTable.setJoinColumns(List.of(new JoinColumn("beer_identifier", "identifier_beer")));
    JoinColumn inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("ingredient_code");
    inverseJoinColumn.setReferencedColumn("code");
    joinTable.setInverseJoinColumns(List.of(inverseJoinColumn));

    PostgresFieldConfiguration ingredientsFieldConfiguration = new PostgresFieldConfiguration();
    ingredientsFieldConfiguration.setJoinTable(joinTable);

    typeConfiguration.setFields(new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration(), FIELD_BREWERY,
        breweryFieldConfiguration, FIELD_INGREDIENTS, ingredientsFieldConfiguration)));

    typeConfiguration.setTable("db.beer");

    typeConfiguration.init(typeMappingMock, newObjectTypeDefinition().name("Beer")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_BREWERY)
            .type(newTypeName("Brewery").build())
            .build())
        .fieldDefinition(newFieldDefinition().name("soldPerYear")
            .type(newTypeName(Scalars.GraphQLFloat.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_INGREDIENTS)
            .type(newTypeName("Ingredient").build())
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void postgresTypeConfigurationMock() {
    AbstractTypeConfiguration postgresTypeConfiguration = new PostgresTypeConfiguration();
    when(typeMappingMock.get("Ingredient")).thenReturn(postgresTypeConfiguration);
  }
}
