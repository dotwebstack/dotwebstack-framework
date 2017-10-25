package org.dotwebstack.framework.frontend.openapi.handlers;

import org.dotwebstack.framework.filter.AbstractFilter;
import org.eclipse.rdf4j.model.IRI;

public final class TestFilter extends AbstractFilter {

  public TestFilter(IRI identifier, String name) {
    super(identifier, name);
  }

  @Override
  public String filter(String value, String query) {
    throw new UnsupportedOperationException(
        "filter() method unsupported, mock the Filter interface instead");
  }

}
