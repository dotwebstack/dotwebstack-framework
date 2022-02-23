package org.dotwebstack.framework.core.helpers;

import org.dotwebstack.framework.core.model.ObjectField;

public class ObjectFieldHelper {

  private static final String SYSTEM_FIELD_FORMAT = "%s.$system";

  public static String createSystemAlias(ObjectField objectField) {
    return String.format(SYSTEM_FIELD_FORMAT, objectField.getName());
  }
}
