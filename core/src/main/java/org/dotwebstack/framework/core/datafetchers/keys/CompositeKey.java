package org.dotwebstack.framework.core.datafetchers.keys;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CompositeKey implements Key {

  private final List<FieldKey> fieldKeys;
}
