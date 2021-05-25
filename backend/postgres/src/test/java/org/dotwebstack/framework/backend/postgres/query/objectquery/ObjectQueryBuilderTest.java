package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.AggregateFieldFactory;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.jooq.DSLContext;
import org.jooq.Meta;
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

  @Mock
  DSLContext dslContext;

  @Mock
  Meta meta;

  private ObjectQueryBuilder objectQueryBuilder;

  @BeforeEach
  void beforeAll() {

    objectQueryBuilder = new ObjectQueryBuilder(dslContext, new AggregateFieldFactory());
    when(dslContext.meta()).thenReturn(meta);
  }

  @Test
  void build_CollectionQuery_Default() {
    var collectionQuery = createCollectionQuery("Brewery");
    addScalarField(collectionQuery.getObjectQuery(), "name");

    SelectQueryBuilderResult result = objectQueryBuilder.build(collectionQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_CollectionQuery_PagingCriteria() {
    var collectionQuery = createCollectionQuery("Brewery");
    addScalarField(collectionQuery.getObjectQuery(), "name");

    SelectQueryBuilderResult result = objectQueryBuilder.build(collectionQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQuery_ForScalarFields() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addScalarField(objectQuery, "name");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQuery_Default() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQueryWithKeyCriteria_Default() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addKeyCriteria(objectQuery);

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQueryAddsKeyField_Default() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_ObjectQueryAddsReferenceColumns_Default() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForObjectFieldsWithJoinColumn() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addObjectField(objectQuery, "Beer");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForObjectFieldsWithJoinTable() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
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
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addNestedObjectField(objectQuery, "Beer");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForAggregateFieldsWithJoinTable() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addAggregateObjectField(objectQuery, "BeerAgg");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForAggregateFieldsWithJoinColumn() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addAggregateObjectField(objectQuery, "BeerAgg");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  @Test
  void build_objectQuery_ForAggregateFieldsWithStringJoinOnArray() {
    ObjectQuery objectQuery = createObjectQuery("Brewery");
    addAggregateObjectField(objectQuery, "BeerAgg");

    SelectQueryBuilderResult result = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    resultNonNullAssertions(result);
  }

  private ObjectQuery createObjectQuery(String typeName) {
    PostgresTypeConfiguration type = mock(PostgresTypeConfiguration.class);
    when(type.getTable()).thenReturn(typeName + TABLE_POSTFIX);

    return ObjectQuery.builder()
        .typeConfiguration(type)
        .keyCriteria(new ArrayList<>())
        .scalarFields(new ArrayList<>())
        .objectFields(new ArrayList<>())
        .nestedObjectFields(new ArrayList<>())
        .aggregateObjectFields(new ArrayList<>())
        .build();
  }

  private CollectionQuery createCollectionQuery(String typeName) {
    PostgresTypeConfiguration type = mock(PostgresTypeConfiguration.class);
    when(type.getTable()).thenReturn(typeName + TABLE_POSTFIX);

    return CollectionQuery.builder()
        .objectQuery(createObjectQuery(typeName))
        .build();
  }

  private void addKeyCriteria(ObjectQuery objectQuery) {

  }

  private void addScalarField(ObjectQuery objectQuery, String scalarName) {

    PostgresFieldConfiguration fieldConfiguration = mock(PostgresFieldConfiguration.class);
    when(fieldConfiguration.getName()).thenReturn(scalarName);

    objectQuery.getScalarFields()
        .add(fieldConfiguration);
  }

  private void addObjectField(ObjectQuery objectQuery, String objectName) {

    ObjectQuery field = createObjectQuery(objectName);

    objectQuery.getObjectFields()
        .add(ObjectFieldConfiguration.builder()
            .objectQuery(field)
            .build());
  }

  private void addNestedObjectField(ObjectQuery objectQuery, String nestedObjectName) {

  }

  private void addAggregateObjectField(ObjectQuery objectQuery, String aggregateName) {

  }

  private void resultNonNullAssertions(SelectQueryBuilderResult result) {
    assertThat(result, notNullValue());
    assertThat(result.getQuery(), notNullValue());
    assertThat(result.getContext(), notNullValue());
    assertThat(result.getMapAssembler(), notNullValue());
  }
}
