package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public interface Parameter<T> {

  IRI getIdentifier();

  String getName();

  boolean isRequired();

  // XXX (PvH) Ik had de Javadoc van validate() wel overgenomen en aangepast.
  T handle(@NonNull Map<String, String> parameterValues);

}
