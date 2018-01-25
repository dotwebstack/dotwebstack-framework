package org.dotwebstack.framework.param;

import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

@RequiredArgsConstructor(staticName = "of")
public final class PropertyShape {

  @Getter
  @NonNull
  private final IRI datatype;
  @Getter
  private final Value defaultValue;
  @Getter
  private final Collection<Value> in;
}
