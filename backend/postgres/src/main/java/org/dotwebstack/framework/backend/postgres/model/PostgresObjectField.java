package org.dotwebstack.framework.backend.postgres.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectField extends AbstractObjectField {

  private static final Pattern NAME_PATTERN = Pattern.compile("([a-z])([A-Z])");

  private static final String NAME_REPLACEMENT = "$1_$2";

  private static final String TSV_PREFIX = "_tsv";

  private String column;

  private String tsvColumn;

  private String columnPrefix;

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
      column = NAME_PATTERN.matcher(name)
          .replaceAll(NAME_REPLACEMENT)
          .toLowerCase();
    }

    return column;
  }

  public String getTsvColumn() {
    // Lazy-determine default tsv column name
    if (tsvColumn == null) {
      tsvColumn = getColumn().concat(TSV_PREFIX);
    }

    return tsvColumn;
  }
}
