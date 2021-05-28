package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.jooq.DSLContext;
import org.jooq.Meta;
import org.jooq.MetaProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelectQueryBuilderTest {
  private static final String TABLE_POSTFIX = "Table";

  private static final String COLUMN_POSTFIX = "Column";

  private static final String FIELD_NAME_POSTFIX = "Name";

  DSLContext dslContext;

  @Mock
  Meta meta;

  private SelectQueryBuilder selectQueryBuilder;

  @BeforeEach
  void beforeAll() {
    dslContext = createDslContext();
    selectQueryBuilder = new SelectQueryBuilder(dslContext, new AggregateFieldFactory());
  }

  @Test
  void buildCollectionQuery_returnsSqlQuery_forScalarFields() {
    when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));

    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));

    var typeName = "Brewery";

    var collectionQuery = CollectionRequest.builder()
        .objectRequest(createObjectQuery(typeName, scalarFields))
        .build();

    var result = selectQueryBuilder.build(collectionQuery);

    assertThat(result.getQuery()
        .toString(), equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"breweryTable\" as \"t1\""));
  }

  @Test
  void buildCollectionQuery_returnsSqlQuery_withPagingCriteria() {
    when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));

    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));

    var typeName = "Brewery";

    var collectionQuery = CollectionRequest.builder()
        .objectRequest(createObjectQuery(typeName, scalarFields))
        .pagingCriteria(PagingCriteria.builder()
            .page(1)
            .pageSize(10)
            .build())
        .build();

    var result = selectQueryBuilder.build(collectionQuery);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"breweryTable\" as \"t1\"\n" + "limit 10\n"
            + "offset 1"));
  }

  @Test
  void buildCollectionQuery_returnsSqlQuery_withFilterCriteria() {
    when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));

    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));

    var typeName = "Brewery";

    var collectionQuery = CollectionRequest.builder()
        .objectRequest(createObjectQuery(typeName, scalarFields))
        .filterCriterias(List.of(EqualsFilterCriteria.builder()
            .field(scalarFields.get(0))
            .value("Brewery X")
            .build()))
        .build();

    var result = selectQueryBuilder.build(collectionQuery);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"breweryTable\" as \"t1\"\n"
            + "where \"t1\".\"nameColumn\" = 'Brewery X'"));
  }

  @Test
  void buildObjectQuery_returnsSqlQuery_forScalarFields() {
    when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));

    var objectQuery = createObjectQuery("Brewery", scalarFields);

    var result = selectQueryBuilder.build(objectQuery);

    assertThat(result.getQuery()
        .toString(), equalTo("select \"t1\".\"nameColumn\" as \"x1\"\n" + "from \"breweryTable\" as \"t1\""));
  }

  @Test
  void buildObjectQuery_returnsSqlQuery_withKeyCriteria() {
    when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));

    var typeConfiguration = mockTypeConfiguration("Brewery");

    var objectQuery = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .keyCriteria(List.of(KeyCriteria.builder()
            .values(Map.of("name", "Beer 1"))
            .build()))
        .build();

    var result = selectQueryBuilder.build(objectQuery);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t3\".*,\n" + "  x2\n" + "from (values ('Beer 1')) as \"t2\" (\"x2\")\n"
            + "  left outer join lateral (\n" + "    select \"t1\".\"nameColumn\" as \"x1\"\n"
            + "    from \"breweryTable\" as \"t1\"\n" + "    where \"t1\".\"name\" = \"t2\".\"x2\"\n"
            + "  ) as \"t3\"\n" + "    on 1 = 1"));
  }

  @Test
  @Disabled
  void build_ObjectQueryAddsKeyField_Default() {
    // TODO
  }

  @Test
  @Disabled
  void build_ObjectQueryAddsReferenceColumns_Default() {
    // TODO
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

  @Test
  void buildObjectQuery_returnsSqlQuery_forObjectFieldsWithJoinColumn() {
    when(meta.getTables("BreweryTable")).thenReturn(List.of(new BreweryTable()));
    when(meta.getTables("AddressTable")).thenReturn(List.of(new AddressTable()));

    var addressIdentifierFieldConfiguration = new PostgresFieldConfiguration();
    addressIdentifierFieldConfiguration.setColumn("identifier_address");

    var typeConfiguration = new PostgresTypeConfiguration();
    typeConfiguration.setKeys(List.of());
    typeConfiguration.setTable("Address" + TABLE_POSTFIX);
    typeConfiguration.setFields(Map.of("identifier_address", addressIdentifierFieldConfiguration));

    var joinColumn = createJoinColumn("postal_address", "identifier_address");
    var fieldConfiguration = createPostgresFieldConfiguration(typeConfiguration, List.of(joinColumn));

    var nestedFieldObjectQuery = ObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .objectRequest(ObjectRequest.builder()
            .typeConfiguration(typeConfiguration)
            .scalarFields(List.of(createScalarFieldConfiguration("street")))
            .build())
        .build();

    var objectQuery = ObjectRequest.builder()
        .typeConfiguration(mockTypeConfiguration("Brewery"))
        .objectFields(List.of(nestedFieldObjectQuery))
        .scalarFields(List.of(createScalarFieldConfiguration("name")))
        .build();

    SelectQueryBuilderResult result = selectQueryBuilder.build(objectQuery);

    assertThat(result.getQuery()
        .toString(),
        equalTo("select\n" + "  \"t1\".\"nameColumn\" as \"x1\",\n" + "  \"t3\".*\n"
            + "from \"breweryTable\" as \"t1\"\n" + "  left outer join lateral (\n"
            + "    select \"t2\".\"streetColumn\" as \"x2\"\n" + "    from \"addressTable\" as \"t2\"\n"
            + "    where \"t1\".\"postal_address\" = \"t2\".\"identifier_address\"\n" + "    limit 1\n"
            + "  ) as \"t3\"\n" + "    on 1 = 1"));
  }

  @Test
  void build_objectQuery_ForObjectFieldsWithJoinTable() {
    when(meta.getTables("IngredientTable"))
        .thenReturn(List.of(new org.dotwebstack.framework.backend.postgres.query.objectquery.IngredientTable()));
    when(meta.getTables("BeerIngredientTable"))
        .thenReturn(List.of(new org.dotwebstack.framework.backend.postgres.query.objectquery.BeerIngredientTable()));

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

    var objectQuery = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(List.of(createScalarFieldConfiguration("identifier_ingredient")))
        .build();

    var result = selectQueryBuilder.build(objectQuery, new ObjectSelectContext(List.of(keyCriteria), true));

    assertThat(result.getQuery()
        .toString(),
        equalTo("select \"t1\".\"identifier_ingredientColumn\" as \"x1\"\n" + "from \"ingredientTable\" as \"t1\"\n"
            + "  join \"beerIngredientTable\" as \"t2\"\n"
            + "    on \"t2\".\"ingredient_identifier\" = \"t1\".\"identifier_ingredientColumn\""));
  }

  @Test
  @Disabled
  void build_objectQuery_ForObjectFieldsWithMappedBy() {}

  @Test
  @Disabled
  void build_objectQuery_ForNestedObjects() {}

  @Test
  @Disabled
  void build_objectQuery_ForAggregateFieldsWithJoinTable() {}

  @Test
  @Disabled
  void build_objectQuery_ForAggregateFieldsWithJoinColumn() {}

  @Test
  @Disabled
  void build_objectQuery_ForAggregateFieldsWithStringJoinOnArray() {
    // TODO:
  }

  private ObjectRequest createObjectQuery(String typeName, List<FieldConfiguration> scalarFields) {
    TypeConfiguration<?> typeConfiguration = mockTypeConfiguration(typeName);

    return ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .build();
  }

  private PostgresFieldConfiguration createScalarFieldConfiguration(String scalarName) {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setName(scalarName);
    fieldConfiguration.setColumn(scalarName + COLUMN_POSTFIX);
    return fieldConfiguration;
  }

  private void addNestedObjectField(ObjectRequest objectRequest, String nestedObjectName) {}

  private void addAggregateObjectField(ObjectRequest objectRequest, String aggregateName) {}

  private void assertNonNull(SelectQueryBuilderResult result) {
    assertThat(result, notNullValue());
    assertThat(result.getQuery(), notNullValue());
    assertThat(result.getContext(), notNullValue());
    assertThat(result.getMapAssembler(), notNullValue());
  }

  private DSLContext createDslContext() {
    MetaProvider metaProvider = mock(MetaProvider.class);
    when(metaProvider.provide()).thenReturn(meta);

    DefaultConfiguration configuration = new DefaultConfiguration();
    configuration.setSQLDialect(SQLDialect.POSTGRES);
    configuration.setMetaProvider(metaProvider);

    return new DefaultDSLContext(configuration);
  }

  private PostgresTypeConfiguration mockTypeConfiguration(String typeName) {
    PostgresTypeConfiguration typeConfiguration = mock(PostgresTypeConfiguration.class);
    String tableName = typeName + TABLE_POSTFIX;
    when(typeConfiguration.getTable()).thenReturn(tableName);
    return typeConfiguration;
  }

}
