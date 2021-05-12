package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@RequiredArgsConstructor
public class ObjectFieldConfiguration {
  private final FieldConfiguration field;
  private final ObjectQuery objectQuery;


}
