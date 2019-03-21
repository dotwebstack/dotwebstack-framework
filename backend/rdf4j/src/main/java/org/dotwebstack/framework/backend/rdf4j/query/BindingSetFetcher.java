package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.query.BindingSet;

@RequiredArgsConstructor
public final class BindingSetFetcher implements DataFetcher<String> {

  private final String propertyName;

  @Override
  public String get(DataFetchingEnvironment environment) throws Exception {
    BindingSet bindingSet = environment.getSource();

    if (bindingSet == null) {
      return null;
    }

    if (bindingSet.hasBinding(propertyName)) {
      return bindingSet.getValue(propertyName).stringValue();
    }

    return null;
  }

}
