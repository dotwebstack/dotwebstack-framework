package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.DSL;

public class BeerIngredientTable extends CustomTable<BeerIngredientRecord> {

  private static final long serialVersionUID = 5517434277601288891L;

  public final TableField<BeerIngredientRecord, String> beerIdentifier =
      createField(DSL.name("beer_identifier"), VARCHAR);

  public final TableField<BeerIngredientRecord, String> ingredientIdentifier =
      createField(DSL.name("ingredient_identifier"), VARCHAR);

  public BeerIngredientTable() {
    super(DSL.name("beerIngredientTable"));
  }

  @Override
  public Class<? extends BeerIngredientRecord> getRecordType() {
    return BeerIngredientRecord.class;
  }
}
