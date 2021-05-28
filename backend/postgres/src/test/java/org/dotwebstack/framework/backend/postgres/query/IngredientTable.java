package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.DSL;

public class IngredientTable extends CustomTable<IngredientRecord> {

  private static final long serialVersionUID = 5517434277601288891L;

  public final TableField<IngredientRecord, String> identifierIngredient =
      createField(DSL.name("identifier_ingredientColumn"), VARCHAR);

  public IngredientTable() {
    super(DSL.name("ingredientTable"));
  }

  @Override
  public Class<? extends IngredientRecord> getRecordType() {
    return IngredientRecord.class;
  }
}
