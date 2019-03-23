package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

public final class BindingSetFetcher implements DataFetcher<Object> {

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {
    BindingSet bindingSet = environment.getSource();
    String propertyName = environment.getFieldDefinition().getName();

    if (bindingSet == null || !bindingSet.hasBinding(propertyName)) {
      return null;
    }

    Value bindingValue = bindingSet.getValue(propertyName);

    if (bindingValue instanceof Literal) {
      return ValueUtils.convertLiteral((Literal) bindingValue);
    }

    return bindingValue.stringValue();
  }

}
