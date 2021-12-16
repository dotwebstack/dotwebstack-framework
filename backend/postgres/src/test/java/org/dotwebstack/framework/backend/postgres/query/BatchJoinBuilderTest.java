package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.BatchJoinBuilder.newBatchJoining;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchJoinBuilderTest {
  private final ObjectFieldMapper<Map<String, Object>> fieldMapper = new ObjectMapper();

  private BatchJoinBuilder batchJoinBuilder;

  @BeforeEach
  public void doBefore() {
    batchJoinBuilder = newBatchJoining().fieldMapper(fieldMapper)
        .aliasManager(new AliasManager());
  }

  @Test
  void build_returnsQuery_forMappedBy() {
    var beerType = new PostgresObjectType();
    beerType.setName("Beer");

    var ingredientType = new PostgresObjectType();
    ingredientType.setName("Ingredient");

    var ingredients = new PostgresObjectField();
    ingredients.setName("ingredients");
    ingredients.setJoinTable(createJoinTable());
    ingredients.setObjectType(ingredientType);
    beerType.getFields()
        .put("ingredients", ingredients);

    var partsOf = new PostgresObjectField();
    partsOf.setName("partsOf");
    partsOf.setObjectType(ingredientType);
    partsOf.setTargetType(beerType);

    var joinConfiguration = JoinConfiguration.builder()
        .objectField(partsOf)
        .mappedBy(ingredients)
        .objectType(ingredientType)
        .targetType(beerType)
        .build();

    var table = DSL.table("ingredients");

    var dataQuery = DSL.select(DSL.asterisk())
        .from(table)
        .getQuery();

    batchJoinBuilder.table(table)
        .joinConfiguration(joinConfiguration)
        .dataQuery(dataQuery)
        .joinKeys(Set.of(Map.of("identifier", "id-1")));

    var result = batchJoinBuilder.build();

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("select *\n" + "from (values ('id-1')) as \"x3\" (\"x2\")\n" + "  left outer join lateral (\n"
            + "    select\n" + "      *,\n" + "      \"x1\".\"ingredient__identifier\"\n" + "    from\n"
            + "      ingredients,\n" + "      \"beer_ingredients\" as \"x1\"\n" + "    where (\n"
            + "      \"x1\".\"beer__identifier\" = \"ingredients\".\"identifier\"\n"
            + "      and \"x1\".\"ingredient__identifier\" = \"x3\".\"x2\"\n" + "    )\n" + "  ) as \"x4\"\n"
            + "    on true"));

  }

  private JoinTable createJoinTable() {
    var joinTable = new JoinTable();
    joinTable.setName("beer_ingredients");

    List<JoinColumn> joinColumns = new ArrayList<>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("beer__identifier");
    joinColumn.setReferencedColumn("identifier");
    joinColumns.add(joinColumn);

    List<JoinColumn> inverseJoinColumns = new ArrayList<>();
    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("ingredient__identifier");
    inverseJoinColumn.setReferencedColumn("identifier");
    inverseJoinColumns.add(inverseJoinColumn);

    joinTable.setJoinColumns(joinColumns);
    joinTable.setInverseJoinColumns(inverseJoinColumns);

    return joinTable;
  }

}
