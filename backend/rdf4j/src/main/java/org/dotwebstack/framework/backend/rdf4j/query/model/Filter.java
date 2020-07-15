package org.dotwebstack.framework.backend.rdf4j.query.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Data
@Builder
public class Filter {

  private FilterOperator operator;

  @Builder.Default
  private List<Operand> operands = new ArrayList<>();

  private Variable variable;

}
