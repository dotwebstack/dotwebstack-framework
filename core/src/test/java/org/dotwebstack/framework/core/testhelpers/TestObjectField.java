package org.dotwebstack.framework.core.testhelpers;

import com.google.common.base.CaseFormat;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TestObjectField extends AbstractObjectField {

  private String column;

  @Valid
  @Builder.Default
  private List<TestJoinColumn> joinColumns = new ArrayList<>();

  @Valid
  private TestJoinTable joinTable;

  private String mappedBy;

  private TestObjectField mappedByObjectField;

  public String getColumn() {
    // Lazy-determine default column name
    if (column == null) {
      column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    return column;
  }
}
