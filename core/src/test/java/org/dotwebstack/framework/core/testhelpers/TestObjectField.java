package org.dotwebstack.framework.core.testhelpers;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestObjectField extends AbstractObjectField {


  private String column;

  @Valid
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
