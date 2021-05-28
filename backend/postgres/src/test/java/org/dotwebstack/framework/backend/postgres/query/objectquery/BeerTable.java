package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.jooq.impl.SQLDataType.VARCHAR;

import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.DSL;

public class BeerTable extends CustomTable<BeerRecord> {

  private static final long serialVersionUID = 5517434277601288891L;

  public final TableField<BeerRecord, String> identifierBeer = createField(DSL.name("identifier_beer"), VARCHAR);

  public final TableField<BeerRecord, String> name = createField(DSL.name("nameColumn"), VARCHAR);

  public BeerTable() {
    super(DSL.name("beerTable"));
  }

  @Override
  public Class<? extends BeerRecord> getRecordType() {
    return BeerRecord.class;
  }
}
