package org.dotwebstack.framework.param;

import java.util.Collection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class ShaclShape {

  @NonNull
  IRI datatype;

  Value defaultValue;

  @NonNull
  Collection<Value> in;
}

