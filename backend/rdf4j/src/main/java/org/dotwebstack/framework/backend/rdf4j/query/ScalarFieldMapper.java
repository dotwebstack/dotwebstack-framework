package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.Getter;
import org.eclipse.rdf4j.query.BindingSet;

@Getter
public class ScalarFieldMapper implements FieldMapper<Object> {

  private final String alias;

  public ScalarFieldMapper(String alias) {
    this.alias = alias;
  }

  @Override
  public Object apply(BindingSet bindings) {
    if (!bindings.hasBinding(alias)) {
      return null;
    }

    return bindings.getBinding(alias)
        .getValue()
        .stringValue();
  }
}
