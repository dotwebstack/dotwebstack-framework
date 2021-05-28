package org.dotwebstack.framework.backend.postgres.query.objectquery;

import org.jooq.Table;
import org.jooq.impl.CustomRecord;

public class IngredientRecord extends CustomRecord<IngredientRecord> {
  private static final long serialVersionUID = 1627085156540801842L;

  protected IngredientRecord(Table<IngredientRecord> table) {
    super(table);
  }
}
