package org.dotwebstack.framework.backend.postgres.query;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.DSL;

public class BreweryTable extends CustomTable<BreweryRecord> {

  private static final long serialVersionUID = 5517434277601288891L;

  public final TableField<BreweryRecord, String> identifier = createField(DSL.name("identifierColumn"), VARCHAR);

  public final TableField<BreweryRecord, String> name = createField(DSL.name("nameColumn"), VARCHAR);

  public final TableField<BreweryRecord, String> postalAddress = createField(DSL.name("postal_address"), VARCHAR);


  public BreweryTable() {
    super(DSL.name("breweryTable"));
  }

  @Override
  public Class<? extends BreweryRecord> getRecordType() {
    return BreweryRecord.class;
  }
}
