package org.dotwebstack.framework.core.config;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class FieldConfiguration {

  private final String mappedBy;
}
