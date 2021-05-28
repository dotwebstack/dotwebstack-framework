package org.dotwebstack.framework.backend.postgres.query.objectquery;

import org.jooq.Table;
import org.jooq.impl.CustomRecord;

public class BeerIngredientRecord extends CustomRecord<BeerIngredientRecord> {
  private static final long serialVersionUID = 1627085156540801842L;

  protected BeerIngredientRecord(Table<BeerIngredientRecord> table) {
    super(table);
  }
}
