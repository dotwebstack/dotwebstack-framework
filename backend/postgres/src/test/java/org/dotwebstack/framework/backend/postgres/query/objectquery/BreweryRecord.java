package org.dotwebstack.framework.backend.postgres.query.objectquery;

import org.jooq.Table;
import org.jooq.impl.CustomRecord;

public class BreweryRecord extends CustomRecord<BreweryRecord> {
  private static final long serialVersionUID = 1627085156540801842L;

  protected BreweryRecord(Table<BreweryRecord> table) {
    super(table);
  }
}
