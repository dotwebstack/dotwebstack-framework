package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FilterTuple {
  private List<String> path;

  private String operator;

  private Object value;
}
