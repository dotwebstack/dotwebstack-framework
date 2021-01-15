package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Rdf4jQueryHolder {

  private final String query;

  private final Map<String, Object> fieldAliasMap;
}
