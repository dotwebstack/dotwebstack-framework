package org.dotwebstack.framework.param;

import java.util.Map;
import org.eclipse.rdf4j.model.IRI;

public interface Parameter {

  IRI getIdentifier();

  String getName();

  Object handle(Map<String, Object> parameterValues);

}
