package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;

@Getter
@Builder
public class RequestContext {

  private final ObjectField objectField;

  private final Map<String, Object> source;
}
