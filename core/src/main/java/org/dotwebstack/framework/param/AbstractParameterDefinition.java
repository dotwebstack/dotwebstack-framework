package org.dotwebstack.framework.param;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Resource;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractParameterDefinition<T extends Parameter<?>>
    implements ParameterDefinition<T> {

  @NonNull
  @Getter
  private final Resource identifier;

  @NonNull
  @Getter
  private final String name;

}
