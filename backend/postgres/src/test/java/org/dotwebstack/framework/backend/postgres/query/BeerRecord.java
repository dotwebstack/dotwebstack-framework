package org.dotwebstack.framework.backend.postgres.query.objectquery;

import org.jooq.Table;
import org.jooq.impl.CustomRecord;

public class BeerRecord extends CustomRecord<BeerRecord> {
  private static final long serialVersionUID = 1627085156540801842L;

  protected BeerRecord(Table<BeerRecord> table) {
    super(table);
  }
}
