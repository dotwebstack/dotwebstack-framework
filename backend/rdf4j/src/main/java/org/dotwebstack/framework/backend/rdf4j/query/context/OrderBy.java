package org.dotwebstack.framework.backend.rdf4j.query.context;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;

@Builder
@Getter
public class OrderBy {
  private FieldPath fieldPath;

  private String order;
}
