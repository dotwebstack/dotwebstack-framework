package org.dotwebstack.framework;

import java.util.Map;
import org.eclipse.rdf4j.model.Resource;

public interface ResourceProvider<R> {

  R get(Resource iri);

  Map<Resource, R> getAll();

}
