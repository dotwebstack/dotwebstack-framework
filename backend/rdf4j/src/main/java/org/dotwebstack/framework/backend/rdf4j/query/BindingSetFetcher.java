package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.eclipse.rdf4j.query.BindingSet;

public final class BindingSetFetcher implements DataFetcher<Object> {

  @Override
  public Object get(DataFetchingEnvironment environment) {
    BindingSet bindingSet = environment.getSource();
    String propertyName = environment.getFieldDefinition().getName();

    if (bindingSet == null || !bindingSet.hasBinding(propertyName)) {
      return null;
    }

    return bindingSet.getValue(propertyName).stringValue();
  }

}
