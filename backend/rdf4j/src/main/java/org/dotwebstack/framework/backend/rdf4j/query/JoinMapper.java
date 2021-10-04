package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.util.Optional;
import org.dotwebstack.framework.core.backend.query.FieldMapper;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

public class JoinMapper implements FieldMapper<BindingSet> {

  private final String alias;

  private final RdfPredicate predicate;

  public JoinMapper(Variable variable, RdfPredicate predicate) {
    this.alias = variable.getQueryString()
        .substring(1);
    this.predicate = predicate;
  }

  @Override
  public Object apply(BindingSet bindings) {
    var resource = Optional.ofNullable(bindings.getValue(alias))
        .map(Resource.class::cast)
        .orElseThrow(() -> illegalStateException("Subject is missing in binding set."));

    return new JoinCondition(resource, predicate);
  }
}
