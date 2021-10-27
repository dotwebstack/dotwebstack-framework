package org.dotwebstack.framework.core.testhelpers;

import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.model.AbstractObjectType;

public class TestObjectType extends AbstractObjectType<TestObjectField> {
  private String table;

  @Override
  public boolean isNested() {
    return StringUtils.isBlank(table);
  }
}
