package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.origin.Origin;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelectQueryBuilderTest {
  private static final String TABLE_POSTFIX = "Table";

  private static final String COLUMN_POSTFIX = "Column";

  DSLContext dslContext;

  private SelectQueryBuilder selectQueryBuilder;

  @BeforeEach
  void beforeAll() {
    dslContext = createDslContext();
    selectQueryBuilder = new SelectQueryBuilder(dslContext, new AggregateFieldFactory());
  }

  @Test
  void buildCollectionRequest_returnsQuery_forScalarFields() {
    var typeConfiguration = mockTypeConfiguration("Brewery");

    var keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField("identifier");

    when(typeConfiguration.getKeys()).thenReturn(List.of(keyConfiguration));

    var identifierFieldConfiguration = new PostgresFieldConfiguration();
    identifierFieldConfiguration.setName("identifier");
    identifierFieldConfiguration.setColumn("identifierColumn");

    when(typeConfiguration.getFields()).thenReturn(Map.of("identifier", identifierFieldConfiguration));

    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeConfiguration, scalarFields))
        .build();

    var result = selectQueryBuilder.build(collectionRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t1\".\"identifierColumn\" as \"x2\"\n"
            + "from \"BreweryTable\" as \"t1\""));
  }

  @Test
  void buildCollectionRequest_returnsQuery_withPagingCriteria() {
    // when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));

    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var typeName = "Brewery";

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeName, scalarFields))
        .pagingCriteria(PagingCriteria.builder()
            .offset(0)
            .first(10)
            .build())
        .build();

    var result = selectQueryBuilder.build(collectionRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"BreweryTable\" as \"t1\"\n" + "limit 10\n"
            + "offset 0"));
  }

  @Test
  void buildCollectionRequest_returnsQuery_withFilterCriteria() {
    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var typeName = "Brewery";

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeName, scalarFields))
        .filterCriterias(List.of(EqualsFilterCriteria.builder()
            .fieldPath(FieldPath.builder()
                .fieldConfiguration((AbstractFieldConfiguration) scalarFields.get(0)
                    .getField())
                .build())
            .value("Brewery X")
            .build()))
        .build();

    var result = selectQueryBuilder.build(collectionRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"BreweryTable\" as \"t1\"\n"
            + "where \"t1\".\"nameColumn\" = 'Brewery X'"));
  }

  @Test
  void buildCollectionRequest_returnsQuery_withSortCriteriaAscending() {
    var fieldConfiguration = createFieldConfiguration("name");

    var sortCriteria = SortCriteria.builder()
        .fieldPath(FieldPath.builder()
            .fieldConfiguration(fieldConfiguration)
            .build())
        .direction(SortDirection.ASC)
        .build();

    Map<String, String> fieldPathAliasMap = new HashMap<>();

    var scalarFields =
        List.of(createScalarFieldConfiguration(fieldConfiguration, Origin.sorting(sortCriteria, fieldPathAliasMap)));

    var typeName = "Brewery";

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeName, scalarFields))
        .sortCriterias(List.of(sortCriteria))
        .build();

    var result = selectQueryBuilder.build(collectionRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"BreweryTable\" as \"t1\"\n"
            + "order by \"t1\".\"nameColumn\" asc"));
    assertThat(fieldPathAliasMap, aMapWithSize(1));
  }

  @Test
  void buildCollectionRequest_returnsQuery_withSortCriteriaDescending() {
    var fieldConfiguration = createFieldConfiguration("name");

    var sortCriteria = SortCriteria.builder()
        .fieldPath(FieldPath.builder()
            .fieldConfiguration(fieldConfiguration)
            .build())
        .direction(SortDirection.DESC)
        .build();

    var typeName = "Brewery";

    Map<String, String> fieldPathAliasMap = new HashMap<>();

    var scalarFields =
        List.of(createScalarFieldConfiguration(fieldConfiguration, Origin.sorting(sortCriteria, fieldPathAliasMap)));

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeName, scalarFields))
        .sortCriterias(List.of(sortCriteria))
        .build();

    var result = selectQueryBuilder.build(collectionRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"BreweryTable\" as \"t1\"\n"
            + "order by \"t1\".\"nameColumn\" desc"));
    assertThat(fieldPathAliasMap, aMapWithSize(1));
  }

  @Test
  void buildObjectRequest_returnsQuery_forScalarFields() {
    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var objectRequest = createObjectRequest("Brewery", scalarFields);

    var result = selectQueryBuilder.build(objectRequest);

    assertThat(result.getQuery()
        .toString(), equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"BreweryTable\" as \"t1\""));
  }

  @Test
  void buildObjectRequest_returnsQuery_withKeyCriteria() {
    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var typeConfiguration = mockTypeConfiguration("Brewery");

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .keyCriteria(List.of(KeyCriteria.builder()
            .values(Map.of("name", "Beer 1"))
            .build()))
        .build();

    var result = selectQueryBuilder.build(objectRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t3\".*,\n" + "  x2\n" + "from (values ('Beer 1')) as \"t2\" (\"x2\")\n"
            + "  left outer join lateral (\n" + "    select \"t1\".\"nameColumn\" as \"x1\"\n"
            + "    from \"BreweryTable\" as \"t1\"\n" + "    where \"t1\".\"name\" = \"t2\".\"x2\"\n"
            + "  ) as \"t3\"\n" + "    on 1 = 1"));
  }

  @Test
  void buildObjectRequest_returnsQuery_forObjectFieldsWithJoinColumn() {
    var addressIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    addressIdentifierFieldConfiguration.setColumn("identifier_address");

    var typeConfiguration = new PostgresTypeConfiguration();
    typeConfiguration.setKeys(List.of());
    typeConfiguration.setTable("Address" + TABLE_POSTFIX);
    typeConfiguration.setFields(Map.of("identifier_address", addressIdentifierFieldConfiguration));

    var joinColumn = createJoinColumn("postal_address", "identifier_address");
    var fieldConfiguration = createPostgresFieldConfiguration(typeConfiguration, List.of(joinColumn));

    var nestedObjectField = ObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .objectRequest(ObjectRequest.builder()
            .typeConfiguration(typeConfiguration)
            .scalarFields(List.of(createScalarFieldConfiguration(createFieldConfiguration("street"))))
            .build())
        .build();

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(mockTypeConfiguration("Brewery"))
        .objectFields(List.of(nestedObjectField))
        .scalarFields(List.of(createScalarFieldConfiguration(createFieldConfiguration("name"))))
        .build();

    SelectQueryBuilderResult result = selectQueryBuilder.build(objectRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t3\".*\n"
            + "from \"BreweryTable\" as \"t1\"\n" + "  left outer join lateral (\n"
            + "    select \"t2\".\"streetColumn\" as \"x2\"\n" + "    from \"AddressTable\" as \"t2\"\n"
            + "    where \"t1\".\"postal_address\" = \"t2\".\"identifier_address\"\n" + "    limit 1\n"
            + "  ) as \"t3\"\n" + "    on 1 = 1"));
  }

  @Test
  void buildObjectRequest_returnsQuery_forObjectFieldsWithJoinTable() {
    var ingredientIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    ingredientIdentifierFieldConfiguration.setColumn("identifier_ingredientColumn");

    var typeConfiguration = new PostgresTypeConfiguration();
    typeConfiguration.setKeys(List.of());
    typeConfiguration.setTable("IngredientTable");
    typeConfiguration.setFields(Map.of("identifier_ingredient", ingredientIdentifierFieldConfiguration));

    var joinTable = new JoinTable();
    joinTable.setName("BeerIngredientTable");
    joinTable.setJoinColumns(List.of(createJoinColumn("beer_identifier", "identifier_beer")));
    joinTable.setInverseJoinColumns(List.of(createJoinColumn("ingredient_identifier", "identifier_ingredient")));

    var fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setType("Ingredient");
    fieldConfiguration.setTypeConfiguration(typeConfiguration);
    fieldConfiguration.setJoinTable(joinTable);

    var keyCriteria = PostgresKeyCriteria.builder()
        .values(Map.of())
        .joinTable(joinTable)
        .build();

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(List.of(createScalarFieldConfiguration(createFieldConfiguration("identifier_ingredient"))))
        .build();

    var result = selectQueryBuilder.build(objectRequest, new ObjectSelectContext(List.of(keyCriteria), true));

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"identifier_ingredientColumn\" as \"x1\"\n" + "from \"IngredientTable\" as \"t1\"\n"
            + "  join \"BeerIngredientTable\" as \"t2\"\n"
            + "    on \"t2\".\"ingredient_identifier\" = \"t1\".\"identifier_ingredientColumn\""));
  }

  @Test
  void buildObjectRequest_returnsQuery_forNestedObjects() {
    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var typeName = "Brewery";

    var typeConfiguration = mockTypeConfiguration(typeName);

    var fieldConfiguration = new PostgresFieldConfiguration();

    var nestedObjectFieldConfiguration = NestedObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .scalarFields(List.of(createScalarFieldConfiguration(createFieldConfiguration("age"))))
        .build();

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .nestedObjectFields(List.of(nestedObjectFieldConfiguration))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectQueryBuilder.build(collectionRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t1\".\"ageColumn\" as \"x2\"\n"
            + "from \"BreweryTable\" as \"t1\""));
  }

  @Test
  void buildObjectRequest_returnsQuery_forAggregateFieldsWithJoinTable() {
    var beerIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    beerIdentifierFieldConfiguration.setColumn("identifier_beer");

    var beerTypeConfiguration = new PostgresTypeConfiguration();
    beerTypeConfiguration.setKeys(List.of());
    beerTypeConfiguration.setTable("BeerTable");
    beerTypeConfiguration.setFields(Map.of("identifierBeer", beerIdentifierFieldConfiguration));

    var joinTable = new JoinTable();
    joinTable.setName("BeerIngredientTable");
    joinTable.setJoinColumns(List.of(createJoinColumn("beer_identifier", "identifierBeer")));
    joinTable.setInverseJoinColumns(List.of(createJoinColumn("ingredient_identifier", "identifier_ingredient")));

    var aggregatePostgresFieldConfiguration = new PostgresFieldConfiguration();
    aggregatePostgresFieldConfiguration.setName("ingredientAgg");
    aggregatePostgresFieldConfiguration.setAggregationOf("Ingredient");
    aggregatePostgresFieldConfiguration.setJoinTable(joinTable);

    var ingredientIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    ingredientIdentifierFieldConfiguration.setColumn("identifier_ingredientColumn");

    var ingredientTypeConfiguration = new PostgresTypeConfiguration();
    ingredientTypeConfiguration.setKeys(List.of());
    ingredientTypeConfiguration.setTable("IngredientTable");
    ingredientTypeConfiguration.setFields(Map.of("identifier_ingredient", ingredientIdentifierFieldConfiguration));

    aggregatePostgresFieldConfiguration.setTypeConfiguration(ingredientTypeConfiguration);

    var ingredientFieldConfiguration = new PostgresFieldConfiguration();
    ingredientFieldConfiguration.setName("weight");
    ingredientFieldConfiguration.setColumn("weight");
    ingredientFieldConfiguration.setType("Int");

    var aggregateFieldConfiguration = AggregateObjectFieldConfiguration.builder()
        .field(aggregatePostgresFieldConfiguration)
        .aggregateFields(List.of(AggregateFieldConfiguration.builder()
            .field(ingredientFieldConfiguration)
            .aggregateFunctionType(AggregateFunctionType.AVG)
            .alias("intAvg")
            .type(ScalarType.INT)
            .build()))
        .build();

    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(beerTypeConfiguration)
        .scalarFields(scalarFields)
        .aggregateObjectFields(List.of(aggregateFieldConfiguration))
        .build();

    var result = selectQueryBuilder.build(objectRequest);
    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t4\".*\n" + "from \"BeerTable\" as \"t1\"\n"
            + "  left outer join lateral (\n" + "    select cast(avg(\"t2\".\"weight\") as int) as \"x2\"\n"
            + "    from \"IngredientTable\" as \"t2\"\n" + "      join \"BeerIngredientTable\" as \"t3\"\n"
            + "        on (\n" + "          \"t3\".\"beer_identifier\" = \"t1\".\"identifier_beer\"\n"
            + "          and \"t3\".\"ingredient_identifier\" = \"t2\".\"identifier_ingredientColumn\"\n"
            + "        )\n" + "  ) as \"t4\"\n" + "    on 1 = 1"));
  }

  @Test
  void buildObjectRequest_returnQuery_forAggregateFieldsWithJoinColumn() {
    var breweryIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    breweryIdentifierFieldConfiguration.setColumn("identifierColumn");

    var breweryTypeConfiguration = new PostgresTypeConfiguration();
    breweryTypeConfiguration.setKeys(List.of());
    breweryTypeConfiguration.setTable("BreweryTable");
    breweryTypeConfiguration.setFields(Map.of("identifier", breweryIdentifierFieldConfiguration));

    var aggregatePostgresFieldConfiguration = new PostgresFieldConfiguration();
    aggregatePostgresFieldConfiguration.setName("beerAgg");
    aggregatePostgresFieldConfiguration.setMappedBy("brewery");
    aggregatePostgresFieldConfiguration.setAggregationOf("Beer");
    aggregatePostgresFieldConfiguration.setJoinColumns(List.of(createJoinColumn("breweryColumn", "identifier")));

    var beerIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    beerIdentifierFieldConfiguration.setColumn("identifierColumn");

    var beerTypeConfiguration = new PostgresTypeConfiguration();
    beerTypeConfiguration.setKeys(List.of());
    beerTypeConfiguration.setTable("BeerTable");
    beerTypeConfiguration.setFields(Map.of("identifier", beerIdentifierFieldConfiguration));

    aggregatePostgresFieldConfiguration.setTypeConfiguration(beerTypeConfiguration);

    var beerFieldConfiguration = new PostgresFieldConfiguration();
    beerFieldConfiguration.setColumn("sold_per_year");
    beerFieldConfiguration.setName("soldPerYear");
    beerFieldConfiguration.setType("Int");

    var aggregateFieldConfiguration = AggregateObjectFieldConfiguration.builder()
        .field(aggregatePostgresFieldConfiguration)
        .aggregateFields(List.of(AggregateFieldConfiguration.builder()
            .field(beerFieldConfiguration)
            .aggregateFunctionType(AggregateFunctionType.AVG)
            .alias("intAvg")
            .type(ScalarType.INT)
            .build()))
        .build();

    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(breweryTypeConfiguration)
        .scalarFields(scalarFields)
        .aggregateObjectFields(List.of(aggregateFieldConfiguration))
        .build();

    var result = selectQueryBuilder.build(objectRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t3\".*\n"
            + "from \"BreweryTable\" as \"t1\"\n" + "  left outer join lateral (\n"
            + "    select cast(avg(\"t2\".\"sold_per_year\") as int) as \"x2\"\n" + "    from \"BeerTable\" as \"t2\"\n"
            + "    where \"t2\".\"breweryColumn\" = \"t1\".\"identifierColumn\"\n" + "  ) as \"t3\"\n"
            + "    on 1 = 1"));
  }

  @Test
  void buildObjectRequest_returnsQuery_forAggregateFieldsWithStringJoinOnArray() {
    var breweryIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    breweryIdentifierFieldConfiguration.setColumn("identifierColumn");

    var breweryTypeConfiguration = new PostgresTypeConfiguration();
    breweryTypeConfiguration.setKeys(List.of());
    breweryTypeConfiguration.setTable("BreweryTable");
    breweryTypeConfiguration.setFields(Map.of("identifier", breweryIdentifierFieldConfiguration));

    var aggregatePostgresFieldConfiguration = new PostgresFieldConfiguration();
    aggregatePostgresFieldConfiguration.setName("beerAgg");
    aggregatePostgresFieldConfiguration.setMappedBy("brewery");
    aggregatePostgresFieldConfiguration.setAggregationOf("Beer");
    aggregatePostgresFieldConfiguration.setJoinColumns(List.of(createJoinColumn("breweryColumn", "identifier")));

    var beerIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    beerIdentifierFieldConfiguration.setColumn("identifierColumn");

    var beerTypeConfiguration = new PostgresTypeConfiguration();
    beerTypeConfiguration.setKeys(List.of());
    beerTypeConfiguration.setTable("BeerTable");
    beerTypeConfiguration.setFields(Map.of("identifier", beerIdentifierFieldConfiguration));

    aggregatePostgresFieldConfiguration.setTypeConfiguration(beerTypeConfiguration);

    var beerFieldConfiguration = new PostgresFieldConfiguration();
    beerFieldConfiguration.setColumn("taste");
    beerFieldConfiguration.setName("taste");
    beerFieldConfiguration.setType("String");
    beerFieldConfiguration.setList(true);

    var aggregateFieldConfiguration = AggregateObjectFieldConfiguration.builder()
        .field(aggregatePostgresFieldConfiguration)
        .aggregateFields(List.of(AggregateFieldConfiguration.builder()
            .field(beerFieldConfiguration)
            .aggregateFunctionType(AggregateFunctionType.JOIN)
            .alias("stringJoin")
            .type(ScalarType.STRING)
            .build()))
        .build();

    List<ScalarField> scalarFields = List.of(createScalarFieldConfiguration(createFieldConfiguration("name")));

    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(breweryTypeConfiguration)
        .scalarFields(scalarFields)
        .aggregateObjectFields(List.of(aggregateFieldConfiguration))
        .build();

    var result = selectQueryBuilder.build(objectRequest);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t3\".*\n"
            + "from \"BreweryTable\" as \"t1\"\n" + "  left outer join lateral (\n"
            + "    select string_agg(cast(\"x2\" as varchar), ',') as \"x2\"\n" + "    from \"BeerTable\" as \"t2\"\n"
            + "      cross join unnest(\"t2\".\"taste\") as \"x2\" (\"COLUMN_VALUE\")\n"
            + "    where \"t2\".\"breweryColumn\" = \"t1\".\"identifierColumn\"\n" + "  ) as \"t3\"\n"
            + "    on 1 = 1"));
  }

  private ObjectRequest createObjectRequest(String typeName, List<ScalarField> scalarFields) {
    TypeConfiguration<?> typeConfiguration = mockTypeConfiguration(typeName);

    return createObjectRequest(typeConfiguration, scalarFields);
  }

  private ObjectRequest createObjectRequest(TypeConfiguration<?> typeConfiguration, List<ScalarField> scalarFields) {
    return ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .build();
  }

  private PostgresFieldConfiguration createPostgresFieldConfiguration(PostgresTypeConfiguration typeConfiguration,
      List<JoinColumn> joinColumns) {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setType(typeConfiguration.getName());
    fieldConfiguration.setTypeConfiguration(typeConfiguration);
    fieldConfiguration.setJoinColumns(joinColumns);
    return fieldConfiguration;
  }

  private JoinColumn createJoinColumn(String name, String referencedField) {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName(name);
    joinColumn.setReferencedField(referencedField);
    return joinColumn;
  }

  private ScalarField createScalarFieldConfiguration(PostgresFieldConfiguration fieldConfiguration) {
    return createScalarFieldConfiguration(fieldConfiguration, Origin.requested());
  }

  private ScalarField createScalarFieldConfiguration(PostgresFieldConfiguration fieldConfiguration, Origin origin) {
    return ScalarField.builder()
        .field(fieldConfiguration)
        .origins(Sets.newHashSet(origin))
        .build();
  }

  private PostgresFieldConfiguration createFieldConfiguration(String scalarName) {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setName(scalarName);
    fieldConfiguration.setColumn(scalarName + COLUMN_POSTFIX);

    return fieldConfiguration;
  }

  private DSLContext createDslContext() {
    DefaultConfiguration configuration = new DefaultConfiguration();
    configuration.setSQLDialect(SQLDialect.POSTGRES);

    return new DefaultDSLContext(configuration);
  }

  private PostgresTypeConfiguration mockTypeConfiguration(String typeName) {
    PostgresTypeConfiguration typeConfiguration = mock(PostgresTypeConfiguration.class);
    String tableName = typeName + TABLE_POSTFIX;
    when(typeConfiguration.getTable()).thenReturn(tableName);
    return typeConfiguration;
  }
}
