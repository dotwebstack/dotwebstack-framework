package org.dotwebstack.framework.backend.rdf4j.expression;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;


@Getter
@Setter
@Builder
public class ExpressionContext {

  // the subject on which the filter is applied
  Variable subject;

  // the operator that is used for the expression
  FilterOperator operator;

  // the object used for the expression
  List<Operand> operands;

  // how this expression is joined with other expressions
  FilterJoinType joinType;

  // the parent to which this expression is connected
  Variable parent;

  NodeShape nodeShape;

  PropertyShape propertyShape;

}
