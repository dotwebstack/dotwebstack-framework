package org.dotwebstack.framework.backend.postgres.query.objectquery;

import org.jooq.Table;
import org.jooq.impl.CustomRecord;

public class AddressRecord extends CustomRecord<AddressRecord> {
  private static final long serialVersionUID = 1627085156540801842L;

  protected AddressRecord(Table<AddressRecord> table) {
    super(table);
  }
}
