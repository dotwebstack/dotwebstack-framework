package org.dotwebstack.framework.backend.postgres.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectField extends AbstractObjectField {

  private String column;

  private String tsvColumn;

  @Valid
  private List<JoinColumn> joinColumns = new ArrayList<>();

  @Valid
  private JoinTable joinTable;

  private String mappedBy;

  private PostgresObjectField mappedByObjectField;

  @JsonIgnore
  private PostgresSpatial spatial;

  public String getColumn() {
    // Lazy-determine default column name
    if (column == null) {
      column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    return column;
  }
}
