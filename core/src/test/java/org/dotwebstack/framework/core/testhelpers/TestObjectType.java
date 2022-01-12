package org.dotwebstack.framework.core.testhelpers;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.model.AbstractObjectType;

public class TestObjectType extends AbstractObjectType<TestObjectField> {

  @Setter
  @Getter
  private String table;

  @Override
  public boolean isNested() {
    return StringUtils.isBlank(table);
  }

  public void setFields(Map<String, TestObjectField> fields) {
    this.fields = fields;
  }
}
