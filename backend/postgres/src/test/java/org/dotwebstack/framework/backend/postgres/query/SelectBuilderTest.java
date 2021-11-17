package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.SelectBuilder.newSelect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.ContextField;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.ScalarType;
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

  @BeforeEach
  void doBefore() {
    fieldMapper = new ObjectMapper();
    selectBuilder = newSelect().aliasManager(new AliasManager())
        .requestContext(requestContext)
        .fieldMapper(fieldMapper);
  }

  @Test
  void build_returnsSelectQuery_forBatchRequest() {
    var requestObjectField = createObjectField("node");

    when(requestContext.getObjectField()).thenReturn(requestObjectField);

    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .build()))
        .build();

    var keys = new HashSet<Map<String, Object>>();
    keys.add(Map.of("identifier", "id-1"));
    keys.add(Map.of("identifier", "id-2"));

    var batchRequest = BatchRequest.builder()
        .objectRequest(objectRequest)
        .keys(keys)
        .build();

    var result = selectBuilder.build(batchRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select *\n" + "from (values\n" + "  ('id-2'),\n" + "  ('id-1')\n" + ") as \"x4\" (\"identifier\")\n"
            + "  left outer join lateral (\n" + "    select\n" + "      \"x1\".\"name_column\" as \"x2\",\n"
            + "      \"x1\".\"soldPerYear_column\" as \"x3\"\n" + "    from \"beer\" as \"x1\"\n"
            + "    where \"identifier\" = \"x4\".\"identifier\"\n" + "  ) as \"x5\"\n" + "    on true"));
  }

  @Test
  void build_returnsSelectQuery_forObjectRequest() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var nestedObjectType = createObjectType(null, "age");

    var nestedObject = ObjectRequest.builder()
        .objectType(nestedObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("age")
            .build()))
        .build();

    var nestedObjectField = createObjectField("history");
    nestedObjectField.setTargetType(nestedObjectType);
    objectType.getFields()
        .put("history", nestedObjectField);

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .build()))
        .objectFields(Map.of(FieldRequest.builder()
            .name("history")
            .build(), nestedObject))
        .keyCriteria(KeyCriteria.builder()
            .values(Map.of("identifier", "id-1"))
            .build())
        .build();

    var result = selectBuilder.build(objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x1\".\"soldPerYear_column\" as \"x3\",\n"
            + "  \"x7\".*\n" + "from \"beer\" as \"x1\"\n" + "  left outer join lateral (\n" + "    select\n"
            + "      \"x1\".\"age_column\" as \"x6\",\n" + "      1 as \"x4\"\n" + "  ) as \"x7\"\n" + "    on true\n"
            + "where \"x1\".\"identifier_column\" = 'id-1'"));
  }

  @Test
  void build_returnsSelectQuery_forObjectRequestWithContextCriteria() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .contextCriteria(createContextCriteria())
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .build()))
        .keyCriteria(KeyCriteria.builder()
            .values(Map.of("identifier", "id-1"))
            .build())
        .build();

    var result = selectBuilder.build(objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select\n" + "  \"x1\".\"name_column\" as \"x2\",\n" + "  \"x1\".\"soldPerYear_column\" as \"x3\"\n"
            + "from beer_History_ctx('validFrom_value') as \"x1\"\n" + "where \"x1\".\"identifier_column\" = 'id-1'"));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequest() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
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
  void build_returnsSelectQuery_forCollectionRequestWithObjectListJoinTable() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var ingredientObjectType = createObjectType("ingredient", "name");

    var ingredientsObjectField = createObjectField("ingredients");
    ingredientsObjectField.setJoinTable(createIngredientsJoinTable());
    ingredientsObjectField.setTargetType(ingredientObjectType);
    ingredientsObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("ingredients", ingredientsObjectField);

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .build()))
        .objectListFields(Map.of(FieldRequest.builder()
            .name("ingredients")
            .build(),
            CollectionRequest.builder()
                .objectRequest(ObjectRequest.builder()
                    .objectType(ingredientObjectType)
                    .scalarFields(List.of(FieldRequest.builder()
                        .name("name")
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

    var beersObjectRequest = ObjectRequest.builder()
        .objectType(beerObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build()))
        .build();

    var beersCollectionRequest = CollectionRequest.builder()
        .objectRequest(beersObjectRequest)
        .build();

    objectType.getFields()
        .put("beers", beersObjectField);

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build()))
        .objectListFields(Map.of(FieldRequest.builder()
            .name("beers")
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
    beersJoinColumn.setReferencedField("brewery_column");
    beersObjectField.setJoinColumns(List.of(beersJoinColumn));

    var objectType = createObjectType("brewery", "identifier", "name");
    beersObjectField.setObjectType(objectType);

    var beerObjectType = createObjectType("beer", "identifier", "name");

    var beersObjectRequest = ObjectRequest.builder()
        .objectType(beerObjectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build()))
        .build();

    var beersCollectionRequest = CollectionRequest.builder()
        .objectRequest(beersObjectRequest)
        .build();

    objectType.getFields()
        .put("beers", beersObjectField);

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
            .build()))
        .objectListFields(Map.of(FieldRequest.builder()
            .name("beers")
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
    assertThat(fieldMapperResult, hasEntry(equalTo("identifier"), equalTo("id-brewery-1")));
    assertThat(fieldMapperResult, hasEntry(equalTo("$join:beers"), equalTo(JoinCondition.builder()
        .key(Map.of("identifier", "id-brewery-1"))
        .build())));
    assertThat(fieldMapperResult, hasEntry(equalTo("name"), equalTo("my brewery")));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithAggregate() {
    var objectType = createObjectType("beer", "identifier", "name", "soldPerYear");

    var ingredientObjectType = createObjectType("ingredient", "identifier", "name", "weight");

    var ingredientsObjectField = createObjectField("ingredients");
    ingredientsObjectField.setJoinTable(createIngredientsJoinTable());
    ingredientsObjectField.setTargetType(ingredientObjectType);
    ingredientsObjectField.setObjectType(objectType);

    objectType.getFields()
        .put("ingredients", ingredientsObjectField);

    var aggregateObjectField = createObjectField("ingredientAgg");
    aggregateObjectField.setAggregationOfType(ingredientObjectType);
    aggregateObjectField.setJoinTable(createIngredientsJoinTable());
    aggregateObjectField.setObjectType(objectType);

    var aggregateField = AggregateField.builder()
        .functionType(AggregateFunctionType.SUM)
        .type(ScalarType.INT)
        .field(ingredientObjectType.getField("weight"))
        .build();

    var aggregateObjectRequest = AggregateObjectRequest.builder()
        .objectField(aggregateObjectField)
        .aggregateFields(List.of(aggregateField))
        .build();

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .aggregateObjectFields(List.of(aggregateObjectRequest))
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .build();

    var result = selectBuilder.build(collectionRequest, null);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select \"x5\".*\n" + "from \"beer\" as \"x1\"\n" + "  left outer join lateral (\n"
            + "    select cast(sum(\"x2\".\"weight_column\") as int) as \"x3\"\n" + "    from\n"
            + "      \"ingredient\" as \"x2\",\n" + "      \"beer_ingredient\" as \"x4\"\n" + "    where (\n"
            + "      \"x4\".\"beer_identifier\" = \"x1\".\"identifier_column\"\n"
            + "      and \"x4\".\"ingredient_identifier\" = \"identifier_column\"\n" + "    )\n" + "  ) as \"x5\"\n"
            + "    on true"));
  }

  @Test
  void build_returnsSelectQuery_forCollectionRequestWithJoinCriteria() {
    var beerObjectType = createObjectType("beer", "identifier");

    var requestObjectField = createObjectField("ingredients");
    requestObjectField.setJoinTable(createIngredientsJoinTable());
    requestObjectField.setObjectType(beerObjectType);

    when(requestContext.getObjectField()).thenReturn(requestObjectField);

    var objectType = createObjectType("ingredient", "identifier", "name");

    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(List.of(FieldRequest.builder()
            .name("name")
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
        .build();

    var result = selectBuilder.build(collectionRequest, joinCriteria);

    assertThat(result, notNullValue());

    assertThat(result.toString(),
        equalTo("select *\n" + "from (values\n" + "  ('id-beer-1'),\n" + "  ('id-beer-2')\n"
            + ") as \"x4\" (\"identifier_column\")\n" + "  left outer join lateral (\n" + "    select\n"
            + "      \"x1\".\"name_column\" as \"x2\",\n" + "      \"x3\".\"beer_identifier\"\n" + "    from\n"
            + "      \"ingredient\" as \"x1\",\n" + "      \"beer_ingredient\" as \"x3\"\n" + "    where (\n"
            + "      \"x3\".\"ingredient_identifier\" = \"x1\".\"identifier_column\"\n"
            + "      and \"x3\".\"beer_identifier\" = \"x4\".\"identifier_column\"\n" + "    )\n" + "  ) as \"x5\"\n"
            + "    on true"));
  }

  private PostgresObjectType createObjectType(String table, String... fields) {
    var objectType = new PostgresObjectType();
    objectType.setTable(table);

    var fieldMap = Stream.of(fields)
        .collect(Collectors.toMap(field -> field, this::createObjectField));

    objectType.setFields(fieldMap);

    return objectType;
  }

  private JoinTable createIngredientsJoinTable() {
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
    inverseJoinColumn.setReferencedField("identifier");
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
        .values(Map.of("fieldName", String.format("%s_value", "validFrom")))
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
