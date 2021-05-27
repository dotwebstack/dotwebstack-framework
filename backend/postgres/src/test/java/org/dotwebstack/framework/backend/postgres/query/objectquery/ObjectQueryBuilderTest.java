package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.AggregateFieldFactory;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Meta;
import org.jooq.MetaProvider;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

@Disabled("TODO")
class ObjectQueryBuilderTest {
  private static final String TABLE_POSTFIX = "Table";

  private static final String COLUMN_POSTFIX = "Column";

  private static final String FIELD_NAME_POSTFIX = "Name";

  DSLContext dslContext;

  @Mock
  Meta meta;

  private ObjectQueryBuilder objectQueryBuilder;

  @BeforeEach
  void beforeAll() {
    dslContext = createDslContext();
    mockTables();
    objectQueryBuilder = new ObjectQueryBuilder(dslContext, new AggregateFieldFactory());
  }

  private void mockTables() {
    var breweryTable = new BreweryTable();
    when(meta.getTables("BreweryTable")).thenReturn(List.of(breweryTable));
  }

  @Test
  void build_CollectionQuery_Default() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    var collectionQuery = createCollectionQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(collectionQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_CollectionQuery_WithPagingCriteria() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    var collectionQuery = createCollectionQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(collectionQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_CollectionQuery_WithKeyConditions() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    var collectionQuery = createCollectionQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(collectionQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQuery_ForScalarFields() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQuery_Default() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQueryWithKeyCriteria_Default() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addKeyCriteria(objectQuery);
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQueryAddsKeyField_Default() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQueryAddsReferenceColumns_Default() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForObjectFieldsWithJoinColumn() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addObjectField(objectQuery, "Beer");
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForObjectFieldsWithJoinTable() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addObjectField(objectQuery, "Beer");
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForObjectFieldsWithMappedBy() {
    // the same as with joincolumn?
  }

  @Test
  void build_objectQuery_ForNestedObjects() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addNestedObjectField(objectQuery, "Beer");
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForAggregateFieldsWithJoinTable() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addAggregateObjectField(objectQuery, "BeerAgg");
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForAggregateFieldsWithJoinColumn() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addAggregateObjectField(objectQuery, "BeerAgg");
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForAggregateFieldsWithStringJoinOnArray() {
    List<FieldConfiguration> scalarFields = List.of(createScalarFieldConfiguration("name"));
    ObjectQuery objectQuery = createObjectQuery("Brewery", scalarFields);
    addAggregateObjectField(objectQuery, "BeerAgg");
    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());
    resultNonNullAssertions(result);
  }

  private ObjectQuery createObjectQuery(String typeName, List<FieldConfiguration> scalarFields) {
    PostgresTypeConfiguration type = mock(PostgresTypeConfiguration.class);
    String tableName = typeName + TABLE_POSTFIX;
    when(type.getTable()).thenReturn(tableName);

    return ObjectQuery.builder()
        .typeConfiguration(type)
        .keyCriteria(new ArrayList<>())
        .scalarFields(scalarFields)
        .objectFields(new ArrayList<>())
        .nestedObjectFields(new ArrayList<>())
        .aggregateObjectFields(new ArrayList<>())
        .collectionObjectFields(new ArrayList<>())
        .build();
  }

  private CollectionQuery createCollectionQuery(String typeName, List<FieldConfiguration> scalarFields) {
    PostgresTypeConfiguration type = mock(PostgresTypeConfiguration.class);
    when(type.getTable()).thenReturn(typeName + TABLE_POSTFIX);
    return CollectionQuery.builder()
        .objectQuery(createObjectQuery(typeName, scalarFields))
        .build();
  }

  private void addKeyCriteria(ObjectQuery objectQuery) {}

  private PostgresFieldConfiguration createScalarFieldConfiguration(String scalarName) {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setName(scalarName);
    fieldConfiguration.setColumn(scalarName + COLUMN_POSTFIX);
    return fieldConfiguration;
  }

  private void addObjectField(ObjectQuery objectQuery, String objectName) {
    ObjectQuery field = createObjectQuery(objectName, new ArrayList<>());
    objectQuery.getObjectFields()
        .add(ObjectFieldConfiguration.builder()
            .objectQuery(field)
            .build());
  }

  private void addNestedObjectField(ObjectQuery objectQuery, String nestedObjectName) {}

  private void addAggregateObjectField(ObjectQuery objectQuery, String aggregateName) {}

  private void resultNonNullAssertions(SelectQueryBuilderResult result) {
    assertThat(result, notNullValue());
    assertThat(result.getQuery(), notNullValue());
    assertThat(result.getContext(), notNullValue());
    assertThat(result.getMapAssembler(), notNullValue());
  }

  private DSLContext createDslContext() {
    Configuration configuration = mock(Configuration.class);
    MetaProvider metaProvider = mock(MetaProvider.class);
    when(configuration.metaProvider()).thenReturn(metaProvider);
    when(metaProvider.provide()).thenReturn(meta);
    return new DefaultDSLContext(configuration);
  }

}
