package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.DSL;

public class AddressTable extends CustomTable<AddressRecord> {

  private static final long serialVersionUID = 5517434277601288891L;

  public final TableField<AddressRecord, String> identifierAddress =
      createField(DSL.name("identifier_address"), VARCHAR);

  public final TableField<AddressRecord, String> street = createField(DSL.name("streetColumn"), VARCHAR);

  public AddressTable() {
    super(DSL.name("addressTable"));
  }

  @Override
  public Class<? extends AddressRecord> getRecordType() {
    return AddressRecord.class;
  }
}
