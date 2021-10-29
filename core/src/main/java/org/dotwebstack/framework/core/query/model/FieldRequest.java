package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FieldRequest {

  private final String name;

  private final boolean isList;
}
