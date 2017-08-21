package org.dotwebstack.framework;

import java.util.Map;
import org.eclipse.rdf4j.model.IRI;

public interface ResourceProvider<R> {

  R get(IRI iri);

  Map<IRI, R> getAll();

}
