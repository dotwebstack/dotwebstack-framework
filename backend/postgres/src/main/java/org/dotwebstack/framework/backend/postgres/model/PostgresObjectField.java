package org.dotwebstack.framework.backend.postgres.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectField extends AbstractObjectField {

  private static final Pattern NAME_PATTERN_1ST = Pattern.compile("([^A-Z])([0-9]*[A-Z])");

  private static final Pattern NAME_PATTERN_2ND = Pattern.compile("([A-Z])([A-Z][^A-Z])");

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

  public void initColumns() {
    if (column == null) {
      var tempName = NAME_PATTERN_1ST.matcher(name)
          .replaceAll(NAME_REPLACEMENT);

      var columnName = NAME_PATTERN_2ND.matcher(tempName)
          .replaceAll(NAME_REPLACEMENT)
          .toLowerCase();

      column = Optional.ofNullable(columnPrefix)
          .map(prefix -> prefix.concat(columnName))
          .orElse(columnName);
    }

    if (tsvColumn == null) {
      tsvColumn = column.concat(TSV_PREFIX);
    }
  }
}
