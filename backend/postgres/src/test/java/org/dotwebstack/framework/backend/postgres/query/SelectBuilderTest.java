package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.SelectBuilder.newSelect;
import static org.dotwebstack.framework.core.backend.filter.FilterCriteriaBuilder.newFilterCriteriaBuilder;
import static org.dotwebstack.framework.core.config.FilterType.EXACT;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.createFieldPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.TestHelper;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.ContextField;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.UnionObjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelectBuilderTest {

  @Mock
  private RequestContext requestContext;

  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private SelectBuilder selectBuilder;

  private TestHelper testHelper = new TestHelper();

  @BeforeEach
  void doBefore() {
    fieldMapper = new ObjectMapper();
    var aliasManager = new AliasManager();
    selectBuilder = newSelect().aliasManager(aliasManager)
        .requestContext(requestContext)
        .tableAlias(aliasManager.newAlias())
        .fieldMapper(fieldMapper);
  }

  @Test
  void build_returnsSelectQuery_forObjectRequest() {
    var objectRequest = getObjectRequestWithNestedObject(null);

    var result = selectBuilder.build((SingleObjectRequest) objectRequest, false);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x1\".\"soldPerYear_column\" as \"x3\",\n"
            + "  \"x1\".\"identifier_column\" as \"x4\",\n" + "  \"x1\".\"age_column\" as \"x5\"\n"
            + "from \"beer\" as \"x1\"\n" + "where \"x1\".\"identifier_column\" = 'id-1'"));
  }

  @Test
  void build_returnsSelectQuery_forDistinctObjectRequest() {
    var objectRequest = getObjectRequestWithNestedObject(null, true);

    var result = selectBuilder.build((SingleObjectRequest) objectRequest, false);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("""
        select distinct
          "x1"."name_column" as "x2",
          "x1"."soldPerYear_column" as "x3",
          "x1"."identifier_column" as "x4",
          "x1"."age_column" as "x5"
        from "beer" as "x1"
        where "x1"."identifier_column" = 'id-1'"""));
  }

  @Test
  void build_returnsSelectQuery_forObjectRequestWithPresenceColumn() {
    var objectRequest = getObjectRequestWithNestedObject("age_column");

    var result = selectBuilder.build((SingleObjectRequest) objectRequest, false);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("""
        select
          "x1"."name_column" as "x2",
          "x1"."soldPerYear_column" as "x3",
          "x1"."identifier_column" as "x4",
          ("x1"."age_column" is not null) as "x5",
          "x1"."age_column" as "x6"
        from "beer" as "x1"
        where "x1"."identifier_column" = 'id-1'"""));
  }

  private ObjectRequest getObjectRequestWithNestedObject(String presenceColumn) {
    return getObjectRequestWithNestedObject(presenceColumn, false);
  }

  private ObjectRequest getObjectRequestWithNestedObject(String presenceColumn, boolean distinct) {
    var objectType = createObjectType("beer", distinct, "identifier", "name", "soldPerYear");

    var nestedObjectType = createObjectType(null, "age");

    var historyObjectField = createObjectField("history");
    historyObjectField.setTargetType(nestedObjectType);
    historyObjectField.setPresenceColumn(presenceColumn);
    objectType.getFields()
        .put("history", historyObjectField);

    var identifierObjectField = createObjectField("identifier");
    objectType.getFields()
        .put("identifier", identifierObjectField);

    var nestedObject = SingleObjectRequest.builder()
        .objectType(nestedObjectType)
        .scalarFields(new ArrayList<>(List.of(FieldRequest.builder()
            .name("age")
            .resultKey("age")
            .build())))
        .build();

    return SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(new ArrayList<>(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .resultKey("soldPerYear")
                .build())))
        .objectFields(Map.of(FieldRequest.builder()
            .name("history")
            .resultKey("history")
            .build(), nestedObject))
        .keyCriterias(List.of(KeyCriteria.builder()
            .fieldPath(List.of(identifierObjectField))
            .value("id-1")
            .build()))
        .build();
  }

  @Test
  void build_returnsSelectQuery_forObjectRequestWithContextCriteria() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var identifierObjectField = createObjectField("identifier");
    objectType.getFields()
        .put("identifier", identifierObjectField);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .contextCriteria(createContextCriteria())
        .scalarFields(new ArrayList<>(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .resultKey("soldPerYear")
                .build())))
        .keyCriterias(List.of(KeyCriteria.builder()
            .fieldPath(List.of(identifierObjectField))
            .value("id-1")
            .build()))
        .build();

    var result = selectBuilder.build(objectRequest, false);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x1\".\"soldPerYear_column\" as \"x3\",\n"
            + "  \"x1\".\"identifier_column\" as \"x4\"\n" + "from beer_History_ctx('validFrom_value') as \"x1\"\n"
            + "where \"x1\".\"identifier_column\" = 'id-1'"));
  }

  @Test
  void build_returnsSelectQuery_forObjectRequestWithKeyCriteriaHavingNodeRelation() {
    var dotWebStackConfiguration = testHelper.init("dotwebstack/dotwebstack-queries-with-keys-using-relations.yaml");
    var breweryObjectType = dotWebStackConfiguration.getObjectType("Brewery")
        .orElseThrow();
    var identifierFieldPath = createFieldPath(breweryObjectType, "identifier");
    var cityFieldPath = createFieldPath(breweryObjectType, "postalAddress.node.city");

    var objectRequest = SingleObjectRequest.builder()
        .objectType(breweryObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("identifier")
            .resultKey("identifier")
            .build()))
        .keyCriterias(List.of(KeyCriteria.builder()
            .fieldPath(identifierFieldPath)
            .value("id-1")
            .build(),
            KeyCriteria.builder()
                .fieldPath(cityFieldPath)
                .value("Dublin")
                .build()))
        .build();

    var result = selectBuilder.build(objectRequest, false);

    var expectedQuery = "select\n" + "  \"x1\".\"identifier\" as \"x2\",\n" + "  \"x1\".\"postal_address\" as \"x3\",\n"
        + "  \"x6\".*\n" + "from \"brewery\" as \"x1\"\n" + "  left outer join lateral (\n" + "    select\n"
        + "      \"x4\".\"city\" as \"x5\",\n" + "      1 as \"x4\"\n" + "    from \"address\" as \"x4\"\n"
        + "    where \"x1\".\"postal_address\" = \"x4\".\"identifier\"\n" + "    limit 1\n" + "  ) as \"x6\"\n"
        + "    on true\n" + "where (\n" + "  \"x1\".\"identifier\" = 'id-1'\n" + "  and \"x5\" = 'Dublin'\n" + ")";
    assertThat(result, notNullValue());
    assertThat(result.toString(), is(expectedQuery));
  }

  @Test
  void build_returnsSelectQuery_forObjectRequestWithKeyCriteriaHavingRefRelation() {
    var dotWebStackConfiguration = testHelper.init("dotwebstack/dotwebstack-queries-with-keys-using-relations.yaml");
    var breweryObjectType = dotWebStackConfiguration.getObjectType("Brewery")
        .orElseThrow();
    var breweryIdentifierFieldPath = createFieldPath(breweryObjectType, "identifier");
    var addressIdentifierFieldPath = createFieldPath(breweryObjectType, "postalAddress.ref.identifier");

    var objectRequest = SingleObjectRequest.builder()
        .objectType(breweryObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("identifier")
            .resultKey("identifier")
            .build()))
        .keyCriterias(List.of(KeyCriteria.builder()
            .fieldPath(breweryIdentifierFieldPath)
            .value("id-1")
            .build(),
            KeyCriteria.builder()
                .value("id-2")
                .fieldPath(addressIdentifierFieldPath)
                .build()))
        .build();

    var result = selectBuilder.build(objectRequest, false);

    var expectedQuery = "select\n" + "  \"x1\".\"identifier\" as \"x2\",\n" + "  \"x1\".\"postal_address\" as \"x3\",\n"
        + "  \"x1\".\"postal_address\" as \"x4\"\n" + "from \"brewery\" as \"x1\"\n" + "where (\n"
        + "  \"x1\".\"identifier\" = 'id-1'\n" + "  and \"x1\".\"postal_address\" = 'id-2'\n" + ")";
    assertThat(result, notNullValue());
    assertThat(result.toString(), is(expectedQuery));
  }

  @Test
  void build_throwsException_forObjectRequestWithKeyCriteriaNotMatchingReferencedField() {
    var dotWebStackConfiguration = testHelper.init("dotwebstack/dotwebstack-queries-with-keys-using-relations.yaml");
    var breweryObjectType = dotWebStackConfiguration.getObjectType("Brewery")
        .orElseThrow();

    var addressIdentifierFieldPath = createFieldPath(breweryObjectType, "postalAddress.ref.identifier");
    var breweryIdentifierFieldPath = createFieldPath(breweryObjectType, "identifier");

    // change name to force mismatch
    addressIdentifierFieldPath.get(2)
        .setName("id");

    var objectRequest = SingleObjectRequest.builder()
        .objectType(breweryObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("identifier")
            .resultKey("identifier")
            .build()))
        .keyCriterias(List.of(KeyCriteria.builder()
            .fieldPath(breweryIdentifierFieldPath)
            .value("id-1")
            .build(),
            KeyCriteria.builder()
                .value("id-2")
                .fieldPath(addressIdentifierFieldPath)
                .build()))
        .build();

    var result = assertThrows(IllegalStateException.class, () -> selectBuilder.build(objectRequest, false));

    assertThat(result.getMessage(),
        is("Can't find a valid joinColumn configuration for '[PostgresObjectField(column=postal_address, "
            + "joinColumns=[JoinColumn(name=postal_address, referencedField=ref.identifier, "
            + "referencedColumn=null)], joinTable=null, mappedBy=null, mappedByObjectField=null, presenceColumn=null, "
            + "spatial=null), PostgresObjectField(column=postal_address__ref, joinColumns=[], joinTable=null, "
            + "mappedBy=null, mappedByObjectField=null, presenceColumn=null, spatial=null), "
            + "PostgresObjectField(column=postal_address__ref__identifier, joinColumns=[], joinTable=null, "
            + "mappedBy=null, mappedByObjectField=null, presenceColumn=null, spatial=null)]'. "
            + "The joinColumn is either empty or does not match the referencedField."));


  }

  @Test
  void build_throwsException_forObjectRequestWithKeyCriteriaEmptyJoinColumns() {
    var dotWebStackConfiguration = testHelper.init("dotwebstack/dotwebstack-queries-with-keys-using-relations.yaml");
    var breweryObjectType = dotWebStackConfiguration.getObjectType("Brewery")
        .orElseThrow();

    var breweryIdentifierFieldPath = createFieldPath(breweryObjectType, "identifier");
    var addressIdentifierFieldPath = createFieldPath(breweryObjectType, "postalAddress.ref.identifier");

    ((PostgresObjectField) breweryObjectType.getField("postalAddress")).setJoinColumns(List.of());

    var objectRequest = SingleObjectRequest.builder()
        .objectType(breweryObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("identifier")
            .resultKey("identifier")
            .build()))
        .keyCriterias(List.of(KeyCriteria.builder()
            .fieldPath(breweryIdentifierFieldPath)
            .value("id-1")
            .build(),
            KeyCriteria.builder()
                .value("id-2")
                .fieldPath(addressIdentifierFieldPath)
                .build()))
        .build();

    var result = assertThrows(IllegalStateException.class, () -> selectBuilder.build(objectRequest, false));

    assertThat(result.getMessage(),
        is("Can't find a valid joinColumn configuration for '[PostgresObjectField(column=postal_address, "
            + "joinColumns=[], joinTable=null, mappedBy=null, mappedByObjectField=null, "
            + "presenceColumn=null, spatial=null), PostgresObjectField(column=postal_address__ref, joinColumns=[], "
            + "joinTable=null, mappedBy=null, mappedByObjectField=null, presenceColumn=null, spatial=null), "
            + "PostgresObjectField(column=postal_address__ref__identifier, joinColumns=[], joinTable=null, "
            + "mappedBy=null, mappedByObjectField=null, presenceColumn=null, spatial=null)]'. "
            + "The joinColumn is either empty or does not match the referencedField."));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequest() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .resultKey("soldPerYear")
                .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n"
        + "  \"x1\".\"soldPerYear_column\" as \"x3\"\n" + "from \"beer\" as \"x1\""));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithUnionObject() {
    var beerObjectType = createObjectType("beer", "identifier", "name", "subRequest");
    beerObjectType.setName("Beer");
    var breweryObjectType = createObjectType("brewery", "identifier", "name");
    breweryObjectType.setName("Brewery");
    var nestedObjectType = createObjectType(null, "age");

    var identifierObjectField = createObjectField("identifier");
    beerObjectType.getFields()
        .put("identifier", identifierObjectField);

    var historyObjectField = createObjectField("history");
    historyObjectField.setTargetType(nestedObjectType);
    historyObjectField.setPresenceColumn(null);
    beerObjectType.getFields()
        .put("history", historyObjectField);

    var nestedObject = SingleObjectRequest.builder()
        .objectType(nestedObjectType)
        .scalarFields(new ArrayList<>(List.of(FieldRequest.builder()
            .name("age")
            .resultKey("age")
            .build())))
        .build();

    var subUnion = UnionObjectRequest.builder()
        .objectRequests(List.of(
            SingleObjectRequest.builder().objectType(beerObjectType).scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build())).build(),
            SingleObjectRequest.builder().objectType(breweryObjectType).scalarFields(List.of(FieldRequest.builder()
                .name("name")
                .resultKey("name")
                .build())).build()))
        .build();

    var beerObjectRequest = SingleObjectRequest.builder()
        .objectType(beerObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectFields(Map.of(FieldRequest.builder()
            .name("history")
            .resultKey("history")
            .build(), nestedObject
            , FieldRequest.builder().name("subRequest").resultKey("subRequest").build(), subUnion
        ))
        .build();

    var breweryObjectRequest = SingleObjectRequest.builder()
        .objectType(breweryObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .build();

    var unionRequest = UnionObjectRequest.builder()
        .objectRequests(List.of(beerObjectRequest, breweryObjectRequest))
        .build();


    var collectionRequest = CollectionRequest.builder()
        .objectRequest(unionRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("""
      select
        "x4".*,
        json_build_object(
          'name_column', "x1"."name_column",
          'history_column', json_build_object('age_column', "x1"."age_column"),
          'dtype', cast('Beer' as varchar)
        ) as "json"
      from "beer" as "x1"
        left outer join lateral (
          select json_build_object('subRequest', json_build_object(
            'name_column', "x2"."name_column",
            'dtype', cast('Beer' as varchar)
          )) as "json"
          from "beer" as "x2"
          union all
          select json_build_object('subRequest', json_build_object(
            'name_column', "x3"."name_column",
            'dtype', cast('Brewery' as varchar)
          )) as "json"
          from "brewery" as "x3"
        ) as "x4"
          on true
      union all
      select json_build_object(
        'name_column', "x3"."name_column",
        'dtype', cast('Brewery' as varchar)
      ) as "json"
      from "brewery" as "x3\""""));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithObjectListJoinTable() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var ingredientObjectType = createObjectType("ingredient", "name");

    var ingredientsObjectField = createObjectField("ingredients");
    ingredientsObjectField.setJoinTable(createIngredientsJoinTable());
    ingredientsObjectField.setTargetType(ingredientObjectType);
    ingredientsObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("ingredients", ingredientsObjectField);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .resultKey("soldPerYear")
                .build()))
        .objectListFields(Map.of(FieldRequest.builder()
            .name("ingredients")
            .resultKey("ingredients")
            .build(),
            CollectionRequest.builder()
                .objectRequest(SingleObjectRequest.builder()
                    .objectType(ingredientObjectType)
                    .scalarFields(List.of(FieldRequest.builder()
                        .name("name")
                        .resultKey("name")
                        .build()))
                    .build())
                .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x1\".\"soldPerYear_column\" as \"x3\",\n"
            + "  \"x1\".\"identifier_column\" as \"x4\"\n" + "from \"beer\" as \"x1\""));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithJoinColumnObject() {
    List<JoinColumn> joinColumns = new ArrayList<>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("postalAddress_column");
    joinColumn.setReferencedColumn("identifier");
    joinColumns.add(joinColumn);

    var postalAddressObjectField = createObjectField("postalAddress");
    postalAddressObjectField.setJoinColumns(joinColumns);

    var addressObjectType = createObjectType("address", "identifier", "street", "city");
    postalAddressObjectField.setTargetType(addressObjectType);

    var objectType = createObjectType("brewery", "identifier", "name");
    postalAddressObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("postalAddress", postalAddressObjectField);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectFields(Map.of(FieldRequest.builder()
            .name("postalAddress")
            .resultKey("postalAddress")
            .build(),
            SingleObjectRequest.builder()
                .objectType(addressObjectType)
                .scalarFields(List.of(FieldRequest.builder()
                    .name("street")
                    .resultKey("street")
                    .build()))
                .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x5\".*\n" + "from \"brewery\" as \"x1\"\n"
            + "  left outer join lateral (\n" + "    select\n" + "      \"x3\".\"street_column\" as \"x4\",\n"
            + "      1 as \"x3\"\n" + "    from \"address\" as \"x3\"\n"
            + "    where \"x1\".\"postalAddress_column\" = \"x3\".\"identifier\"\n" + "    limit 1\n"
            + "  ) as \"x5\"\n" + "    on true"));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithNestedRelationObjectJoinTableRefs() {
    var ingredientRefType = createObjectType(null, "identifier");
    var ingredientObjectType = createObjectType("ingredient", "name");

    var ingredientObjectRelationType = createObjectType(null, "refs", "nodes");
    ingredientObjectRelationType.getField("refs")
        .setTargetType(ingredientRefType);
    ingredientObjectRelationType.getField("nodes")
        .setTargetType(ingredientObjectType);

    var ingredientRelationObjectField = createObjectField("ingredientRelation");
    ingredientRelationObjectField.setJoinTable(createIngredientsJoinTableForRelationObject());
    ingredientRelationObjectField.setTargetType(ingredientObjectRelationType);

    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");
    ingredientRelationObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("ingredientRelation", ingredientRelationObjectField);

    Map<FieldRequest, CollectionRequest> objectListFields = new HashMap<>();
    objectListFields.put(FieldRequest.builder()
        .name("refs")
        .resultKey("refs")
        .build(),
        CollectionRequest.builder()
            .objectRequest(SingleObjectRequest.builder()
                .objectType(ingredientRefType)
                .scalarFields(List.of(FieldRequest.builder()
                    .name("identifier")
                    .resultKey("identifier")
                    .build()))
                .build())
            .build());

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectFields(Map.of(FieldRequest.builder()
            .name("ingredientRelation")
            .resultKey("ingredientRelation.alias")
            .build(),
            SingleObjectRequest.builder()
                .objectListFields(objectListFields)
                .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x5\".*\n"
        + "from \"beer\" as \"x1\"\n" + "  left outer join lateral (\n"
        + "    select array_agg(ingredient_identifier) as \"x4\"\n" + "    from \"beer_ingredient\" as \"x3\"\n"
        + "    where \"x3\".\"beer_identifier\" = \"x1\".\"identifier_column\"\n" + "  ) as \"x5\"\n" + "    on true"));

    var fieldMapperResult = fieldMapper.apply(Map.of("x2", "Brewery 1"));

    assertThat(fieldMapperResult, notNullValue());
    assertThat(fieldMapperResult, hasEntry(equalTo("name"), equalTo("Brewery 1")));
    assertThat(fieldMapperResult, hasKey("ingredientRelation.alias"));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithNestedRelationObjectJoinTableNodes() {
    var ingredientRefType = createObjectType(null, "identifier");
    var ingredientObjectType = createObjectType("ingredient", "name");

    var ingredientObjectRelationType = createObjectType(null, "refs", "nodes");
    ingredientObjectRelationType.getField("refs")
        .setTargetType(ingredientRefType);
    ingredientObjectRelationType.getField("nodes")
        .setTargetType(ingredientObjectType);

    var ingredientRelationObjectField = createObjectField("ingredientRelation");
    ingredientRelationObjectField.setJoinTable(createIngredientsJoinTableForRelationObject());
    ingredientRelationObjectField.setTargetType(ingredientObjectRelationType);

    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");
    ingredientRelationObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("ingredientRelation", ingredientRelationObjectField);

    Map<FieldRequest, CollectionRequest> objectListFields = new HashMap<>();
    objectListFields.put(FieldRequest.builder()
        .name("nodes")
        .resultKey("nodes")
        .build(),
        CollectionRequest.builder()
            .objectRequest(SingleObjectRequest.builder()
                .objectType(ingredientObjectType)
                .scalarFields(List.of(FieldRequest.builder()
                    .name("name")
                    .resultKey("name")
                    .build()))
                .build())
            .build());

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectFields(Map.of(FieldRequest.builder()
            .name("ingredientRelation")
            .resultKey("ingredientRelation")
            .build(),
            SingleObjectRequest.builder()
                .objectListFields(objectListFields)
                .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n"
        + "  \"x1\".\"identifier_column\" as \"x3\"\n" + "from \"beer\" as \"x1\""));

    var fieldMapperResult = fieldMapper.apply(Map.of("x2", "my brewery", "x3", "id-brewery-1"));

    assertThat(fieldMapperResult, notNullValue());
    assertThat(fieldMapperResult, hasEntry(equalTo("identifier"), equalTo("id-brewery-1")));
    assertThat(fieldMapperResult, hasKey("ingredientRelation"));
    assertThat(fieldMapperResult, hasEntry(equalTo("name"), equalTo("my brewery")));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithNestedRelationObjectJoinColumnNode() {
    var breweryRefType = createObjectType(null, "identifier");
    var breweryObjectType = createObjectType("brewery", "identifier", "name");

    var breweryObjectRelationType = createObjectType(null, "ref", "node");
    breweryObjectRelationType.getField("ref")
        .setTargetType(breweryRefType);
    breweryObjectRelationType.getField("node")
        .setTargetType(breweryObjectType);
    breweryObjectRelationType.getField("node")
        .setObjectType(breweryObjectRelationType);

    List<JoinColumn> breweryNodeJoinColumns = new ArrayList<>();
    var breweryNodeJoinColumn = new JoinColumn();
    breweryNodeJoinColumn.setName("brewery_column");
    breweryNodeJoinColumn.setReferencedField("identifier");
    breweryNodeJoinColumns.add(breweryNodeJoinColumn);
    breweryObjectRelationType.getField("node")
        .setJoinColumns(breweryNodeJoinColumns);

    List<JoinColumn> breweryJoinColumns = new ArrayList<>();
    var breweryJoinColumn = new JoinColumn();
    breweryJoinColumn.setName("brewery_column");
    breweryJoinColumn.setReferencedField("ref.identifier");
    breweryJoinColumns.add(breweryJoinColumn);

    var breweryRelationObjectField = createObjectField("breweryRelation");
    breweryRelationObjectField.setJoinColumns(breweryJoinColumns);
    breweryRelationObjectField.setTargetType(breweryObjectRelationType);

    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear", "brewery");
    breweryRelationObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("breweryRelation", breweryRelationObjectField);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectFields(Map.of(FieldRequest.builder()
            .name("breweryRelation")
            .resultKey("breweryRelation")
            .build(),
            SingleObjectRequest.builder()
                .objectType(breweryObjectRelationType)
                .objectFields(Map.of(FieldRequest.builder()
                    .name("node")
                    .resultKey("node")
                    .build(),
                    SingleObjectRequest.builder()
                        .objectType(breweryObjectType)
                        .scalarFields(List.of(FieldRequest.builder()
                            .name("name")
                            .resultKey("name")
                            .build()))
                        .build()))
                .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x1\".\"brewery_column\" as \"x3\",\n"
            + "  \"x6\".*\n" + "from \"beer\" as \"x1\"\n" + "  left outer join lateral (\n" + "    select\n"
            + "      \"x4\".\"name_column\" as \"x5\",\n" + "      1 as \"x4\"\n" + "    from \"brewery\" as \"x4\"\n"
            + "    where \"x1\".\"brewery_column\" = \"x4\".\"identifier_column\"\n" + "    limit 1\n"
            + "  ) as \"x6\"\n" + "    on true"));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithObjectListMappedBy() {
    var breweryObjectField = createObjectField("brewery");

    var breweryJoinColumn = new JoinColumn();
    breweryJoinColumn.setName("brewery_column");
    breweryJoinColumn.setReferencedField("identifier");

    var beerObjectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    breweryObjectField.setJoinColumns(List.of(breweryJoinColumn));
    breweryObjectField.setObjectType(beerObjectType);
    breweryObjectField.setTargetType(beerObjectType);

    var objectType = createObjectType("brewery", "identifier", "name");

    var beersObjectField = createObjectField("beers");
    beersObjectField.setMappedByObjectField(breweryObjectField);
    beersObjectField.setObjectType(objectType);

    var beersObjectRequest = SingleObjectRequest.builder()
        .objectType(beerObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .build();

    var beersCollectionRequest = CollectionRequest.builder()
        .objectRequest(beersObjectRequest)
        .build();

    objectType.getFields()
        .put("beers", beersObjectField);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectListFields(Map.of(FieldRequest.builder()
            .name("beers")
            .resultKey("beers")
            .build(), beersCollectionRequest))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n"
        + "  \"x1\".\"identifier_column\" as \"x3\"\n" + "from \"brewery\" as \"x1\""));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithObjectListJoinColumn() {
    var beersObjectField = createObjectField("beers");
    var beersJoinColumn = new JoinColumn();

    beersJoinColumn.setName("identifier");
    beersJoinColumn.setReferencedColumn("brewery_column");
    beersObjectField.setJoinColumns(List.of(beersJoinColumn));

    var objectType = createObjectType("brewery", "identifier", "name");
    beersObjectField.setObjectType(objectType);

    var beerObjectType = createObjectType("beer", "identifier", "name");

    var beersObjectRequest = SingleObjectRequest.builder()
        .objectType(beerObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .build();

    var beersCollectionRequest = CollectionRequest.builder()
        .objectRequest(beersObjectRequest)
        .build();

    objectType.getFields()
        .put("beers", beersObjectField);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .objectListFields(Map.of(FieldRequest.builder()
            .name("beers")
            .resultKey("beers")
            .build(), beersCollectionRequest))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n"
        + "  \"x1\".\"identifier\" as \"x3\"\n" + "from \"brewery\" as \"x1\""));

    var fieldMapperResult = fieldMapper.apply(Map.of("x2", "my brewery", "x3", "id-brewery-1"));

    assertThat(fieldMapperResult, notNullValue());
    assertThat(fieldMapperResult, hasEntry(equalTo("$join:beers"), equalTo(PostgresJoinCondition.builder()
        .key(Map.of("identifier", "id-brewery-1"))
        .build())));
    assertThat(fieldMapperResult, hasEntry(equalTo("name"), equalTo("my brewery")));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithSumAggregate() {
    var expectedResult = "select \"x5\".*\n" + "from \"beer\" as \"x1\"\n" + "  left outer join lateral (\n"
        + "    select cast(sum(\"x2\".\"weight_column\") as int) as \"x3\"\n" + "    from\n"
        + "      \"ingredient\" as \"x2\",\n" + "      \"beer_ingredient\" as \"x4\"\n" + "    where (\n"
        + "      \"x4\".\"beer_identifier\" = \"x1\".\"identifier_column\"\n"
        + "      and \"x4\".\"ingredient_identifier\" = \"x2\".\"identifier_column\"\n" + "    )\n" + "  ) as \"x5\"\n"
        + "    on true";

    build_returnsSelectQuery_forCollectionRequestWithAggregate(AggregateFunctionType.SUM, ScalarType.INT, "weight",
        expectedResult, null);
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithStringJoinAggregate() {
    var expectedResult = "select \"x5\".*\n" + "from \"beer\" as \"x1\"\n" + "  left outer join lateral (\n"
        + "    select string_agg(cast(\"x2\".\"name_column\" as varchar), ',') as \"x3\"\n" + "    from\n"
        + "      \"ingredient\" as \"x2\",\n" + "      \"beer_ingredient\" as \"x4\"\n" + "    where (\n"
        + "      \"x4\".\"beer_identifier\" = \"x1\".\"identifier_column\"\n"
        + "      and \"x4\".\"ingredient_identifier\" = \"x2\".\"identifier_column\"\n" + "    )\n" + "  ) as \"x5\"\n"
        + "    on true";

    build_returnsSelectQuery_forCollectionRequestWithAggregate(AggregateFunctionType.JOIN, ScalarType.STRING, "name",
        expectedResult, null);
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithStringJoinAggregateAndAggregateFilter() {
    var filterConfig = new FilterConfiguration();
    filterConfig.setName("name");
    filterConfig.setType(EXACT);
    filterConfig.setField("name");
    filterConfig.setCaseSensitive(true);

    var ingredientObjectType = createObjectType("ingredient", "identifier", "name", "weight");
    ingredientObjectType.setFilters(Map.of("name", filterConfig));

    var filterCriteria = (GroupFilterCriteria) newFilterCriteriaBuilder().objectType(ingredientObjectType)
        .argument(Map.of("name", Map.of("in", List.of("Water", "Caramel"))))
        .maxDepth(2)
        .build();

    var expectedResult = "select \"x5\".*\nfrom \"beer\" as \"x1\"\n  left outer join lateral (\n"
        + "    select string_agg(cast(\"x2\".\"name_column\" as varchar), ',') as \"x3\"\n    from\n"
        + "      \"ingredient\" as \"x2\",\n      \"beer_ingredient\" as \"x4\"\n    where (\n"
        + "      \"x4\".\"beer_identifier\" = \"x1\".\"identifier_column\"\n"
        + "      and \"x4\".\"ingredient_identifier\" = \"x2\".\"identifier_column\"\n"
        + "      and \"x2\".\"name_column\" in (\n        'Water', 'Caramel'\n      )\n    )\n  ) as \"x5\"\n"
        + "    on true";

    build_returnsSelectQuery_forCollectionRequestWithAggregate(AggregateFunctionType.JOIN, ScalarType.STRING, "name",
        expectedResult, filterCriteria);
  }

  private void build_returnsSelectQuery_forCollectionRequestWithAggregate(AggregateFunctionType functionType,
      ScalarType fieldType, String fieldName, String expectedResult, GroupFilterCriteria filterCriteria) {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var ingredientObjectType = createObjectType("ingredient", "identifier", "name", "weight");

    var ingredientsObjectField = createObjectField("ingredients");
    ingredientsObjectField.setJoinTable(createIngredientsJoinTable());
    ingredientsObjectField.setTargetType(ingredientObjectType);
    ingredientsObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("ingredients", ingredientsObjectField);

    var aggregateObjectField = createObjectField("ingredientAgg");
    aggregateObjectField.setTargetType(ingredientObjectType);
    aggregateObjectField.setJoinTable(createIngredientsJoinTable());
    aggregateObjectField.setObjectType(objectType);

    var aggregateField = AggregateField.builder()
        .functionType(functionType)
        .type(fieldType)
        .field(ingredientObjectType.getField(fieldName))
        .build();

    var aggregateObjectRequest = AggregateObjectRequest.builder()
        .objectField(aggregateObjectField)
        .aggregateFields(List.of(aggregateField))
        .filterCriteria(filterCriteria)
        .build();

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .aggregateObjectFields(List.of(aggregateObjectRequest))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo(expectedResult));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithJoinCriteria() {
    var beerObjectType = createObjectType("beer", "identifier");

    var requestObjectField = createObjectField("ingredients");
    requestObjectField.setJoinTable(createIngredientsJoinTable());
    requestObjectField.setObjectType(beerObjectType);

    when(requestContext.getObjectField()).thenReturn(requestObjectField);

    var objectType = createObjectType("ingredient", "identifier", "name");

    requestObjectField.setTargetType(objectType);

    var objectRequest = SingleObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .resultKey("name")
            .build()))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var keys = new HashSet<Map<String, Object>>();
    keys.add(Map.of("identifier_column", "id-beer-1"));
    keys.add(Map.of("identifier_column", "id-beer-2"));

    var joinCriteria = JoinCriteria.builder()
        .keys(keys)
        .joinCondition(PostgresJoinCondition.builder()
            .joinTable(createIngredientsJoinTable())
            .build())
        .build();

    var result = selectBuilder.build(collectionRequest, joinCriteria);

    assertThat(result, notNullValue());

    assertThat(result.toString(), equalTo("select *\n" + "from (values\n" + "  ('id-beer-1'),\n" + "  ('id-beer-2')\n"
        + ") as \"x5\" (\"x4\")\n" + "  left outer join lateral (\n" + "    select\n"
        + "      \"x1\".\"name_column\" as \"x2\",\n" + "      \"x3\".\"beer_identifier\" as \"x6\"\n" + "    from\n"
        + "      \"ingredient\" as \"x1\",\n" + "      \"beer_ingredient\" as \"x3\"\n" + "    where (\n"
        + "      \"x3\".\"ingredient_identifier\" = \"x1\".\"identifier_column\"\n"
        + "      and \"x3\".\"beer_identifier\" = \"x5\".\"x4\"\n" + "    )\n" + "  ) as \"x7\"\n" + "    on true"));
  }

  private PostgresObjectType createObjectType(String table, String... fields) {
    return createObjectType(table, false, fields);
  }

  private PostgresObjectType createObjectType(String table, boolean distinct, String... fields) {
    var objectType = new PostgresObjectType();
    objectType.setTable(table);

    var fieldMap = Stream.of(fields)
        .collect(Collectors.toMap(field -> field, this::createObjectField));

    objectType.setFields(fieldMap);

    objectType.setDistinct(distinct);

    return objectType;
  }

  private JoinTable createIngredientsJoinTableForRelationObject() {
    return createIngredientsJoinTable("refs.identifier");
  }

  private JoinTable createIngredientsJoinTable() {
    return createIngredientsJoinTable("identifier");
  }

  private JoinTable createIngredientsJoinTable(String referencedField) {
    var joinTable = new JoinTable();
    joinTable.setName("beer_ingredient");

    List<JoinColumn> joinColumns = new ArrayList<>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("beer_identifier");
    joinColumn.setReferencedField("identifier");
    joinColumns.add(joinColumn);

    List<JoinColumn> inverseJoinColumns = new ArrayList<>();
    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("ingredient_identifier");
    inverseJoinColumn.setReferencedField(referencedField);
    inverseJoinColumns.add(inverseJoinColumn);

    joinTable.setJoinColumns(joinColumns);
    joinTable.setInverseJoinColumns(inverseJoinColumns);

    return joinTable;
  }

  private PostgresObjectField createObjectField(String name) {
    var objectField = new PostgresObjectField();
    objectField.setName(name);
    objectField.setColumn(String.format("%s_column", name));
    return objectField;
  }

  private ContextCriteria createContextCriteria() {
    return ContextCriteria.builder()
        .name("History")
        .context(createContext())
        .values(Map.of("validFrom", String.format("%s_value", "validFrom")))
        .build();
  }

  private Context createContext() {
    var context = new Context();

    var field = new ContextField();
    field.setType("String");
    context.setFields(Map.of("validFrom", field));

    return context;
  }
}
