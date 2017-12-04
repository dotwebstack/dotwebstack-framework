package org.dotwebstack.framework.frontend.ld.parameter.target;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.param.ParameterDefinition;

public class ParameterTarget implements Target {

  private ParameterDefinition parameter;

  public ParameterTarget(ParameterDefinition parameter) {
    this.parameter = parameter;
  }

  public Map<String, Object> set(String value) {

    Map<String, Object> result = new HashMap<String, Object>();

    result.put(parameter.getName(), value);

    return result;
  }
}
