package org.dotwebstack.framework.filter;

import org.eclipse.rdf4j.model.IRI;

public interface Filter {

  IRI getIdentifier();

  String getName();

  String filter(String value, String query);

}
