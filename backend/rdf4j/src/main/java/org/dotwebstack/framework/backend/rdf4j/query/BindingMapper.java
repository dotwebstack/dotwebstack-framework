package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Optional;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

@Getter
class BindingMapper implements ScalarFieldMapper<BindingSet> {

  private final String alias;

  public BindingMapper(String alias) {
    this.alias = alias;
  }

  @Override
  public Object apply(BindingSet bindings) {
    return Optional.ofNullable(bindings.getBinding(alias))
        .map(Binding::getValue)
        .map(Value::stringValue);
  }
}
