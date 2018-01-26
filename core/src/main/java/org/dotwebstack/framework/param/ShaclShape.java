package org.dotwebstack.framework.param;

import java.util.Collection;
import lombok.Data;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

@Data
public final class ShaclShape {

  @NonNull
  private final IRI datatype;

  private final Value defaultValue;

  @NonNull
  private final Collection<Value> in;
}

