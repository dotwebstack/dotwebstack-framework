package org.dotwebstack.framework.backend.postgres.model;

import com.google.common.base.CaseFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectField extends AbstractObjectField {

  private String column;

  public String getColumn() {
    // Lazy-determine default column name
    if (column == null) {
      column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    return column;
  }
}
