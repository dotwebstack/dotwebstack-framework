package org.dotwebstack.framework.param;

import java.util.Map;
import org.eclipse.rdf4j.model.IRI;

public interface Parameter<T> {

  IRI getIdentifier();

  String getName();

  T handle(Map<String, Object> parameterValues);

}
