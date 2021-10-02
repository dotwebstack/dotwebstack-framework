package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.eclipse.rdf4j.query.BindingSet;

@Getter
public class BindingMapper implements ScalarFieldMapper<BindingSet> {

  private final String alias;

  public BindingMapper(String alias) {
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
