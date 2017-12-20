package org.dotwebstack.framework.frontend.ld.parameter.target;

import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterTarget implements Target {

  private static final Logger LOG = LoggerFactory.getLogger(ParameterTarget.class);

  private ParameterDefinition parameter;

  public ParameterTarget(ParameterDefinition parameter) {
    this.parameter = parameter;
  }

  public ImmutableMap<String, String> set(String value) {
    LOG.debug("Set parameter {}: {}", parameter.getName(), value);
    return ImmutableMap.of(parameter.getName(), value);
  }

}
