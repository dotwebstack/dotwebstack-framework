package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.rdf4j.query.BindingSet;

@Builder
@Getter
public class QueryHolder {

  private final String query;

  private final Function<BindingSet, Map<String, Object>> mapAssembler;

  private final Map<String, String> keyColumnNames;
}
