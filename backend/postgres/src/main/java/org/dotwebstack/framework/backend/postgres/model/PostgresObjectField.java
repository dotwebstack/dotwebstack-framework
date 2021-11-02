package org.dotwebstack.framework.backend.postgres.model;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectField extends AbstractObjectField {

  private String column;

  @Valid
  private List<JoinColumn> joinColumns = new ArrayList<>();

  @Valid
  private JoinTable joinTable;

  private String mappedBy;

  private PostgresObjectField mappedByObjectField;

  private Map<Integer, SpatialReferenceSystem> spatialReferenceSystems;

  public String getColumn() {
    // Lazy-determine default column name
    if (column == null) {
      column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    return column;
  }
}
