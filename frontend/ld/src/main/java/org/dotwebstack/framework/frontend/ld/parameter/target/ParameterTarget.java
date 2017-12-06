package org.dotwebstack.framework.frontend.ld.parameter.target;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterTarget implements Target {

  private static final Logger LOG = LoggerFactory.getLogger(ParameterTarget.class);

  private ParameterDefinition parameter;

  public ParameterTarget(ParameterDefinition parameter) {
    this.parameter = parameter;
  }

  public Map<String, Object> set(String value) {

    Map<String, Object> result = new HashMap<String, Object>();

    result.put(parameter.getName(), value);

    LOG.debug("Set parameter {}: {}", parameter.getName(), value);

    return result;
  }
}
