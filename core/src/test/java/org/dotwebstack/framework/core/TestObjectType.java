package org.dotwebstack.framework.core;

import org.dotwebstack.framework.core.model.AbstractObjectType;

public class TestObjectType extends AbstractObjectType<TestObjectField> {
  @Override
  public boolean isNested() {
    return false;
  }
}
