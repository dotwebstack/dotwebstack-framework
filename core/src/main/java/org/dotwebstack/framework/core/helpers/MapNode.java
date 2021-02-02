package org.dotwebstack.framework.core.helpers;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
@Getter
public class MapNode {

  private final TypeConfiguration<?> typeConfiguration;

  private final Map<String, Object> fieldAliasMap;
}
