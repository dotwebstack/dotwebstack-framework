package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Optional;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Getter
class BindingMapper implements ScalarFieldMapper<BindingSet> {

  protected final String alias;

  public BindingMapper(String alias) {
    this.alias = alias;
  }

  public BindingMapper(Variable variable) {
    this.alias = variable.getQueryString()
        .substring(1);
  }

  @Override
  public Object apply(BindingSet bindings) {
    var value = bindings.getValue(alias);

    if (value == null) {
      return Optional.empty();
    }

    return value.stringValue();
  }
}
