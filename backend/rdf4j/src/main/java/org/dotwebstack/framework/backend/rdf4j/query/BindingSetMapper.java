package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Optional;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.AbstractObjectMapper;
import org.eclipse.rdf4j.query.BindingSet;

@Getter
class BindingSetMapper extends AbstractObjectMapper<BindingSet> {

  private final String alias;

  public BindingSetMapper(String alias) {
    this.alias = alias;
  }

  @Override
  public Object apply(BindingSet bindings) {
    if (!bindings.hasBinding(alias)) {
      return Optional.empty();
    }

    return super.apply(bindings);
  }
}
