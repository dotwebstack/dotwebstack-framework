package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.query.context.Constraint;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.junit.jupiter.api.Test;

public class ConstraintTypeTest {

  private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

  @Test
  public void toString_returnsSingleString_forSingleValueConstraint() {
    Constraint constraint = Constraint.builder()
        .constraintType(ConstraintType.RDF_TYPE)
        .predicate(() -> stringify(RDF.TYPE))
        .values(Set.of(VF.createIRI("http://example.com/dotwebstack#Brewery")))
        .build();

    assertThat(constraint.toString(), is(equalTo(stringify(RDF.TYPE) + " http://example.com/dotwebstack#Brewery")));
  }

  @Test
  public void toString_returnsCommaSeparatedString_forMultiValueConstraint() {
    Constraint constraint = Constraint.builder()
        .constraintType(ConstraintType.RDF_TYPE)
        .predicate(() -> stringify(RDF.TYPE))
        .values(Set.of(VF.createIRI("http://example.com/dotwebstack#Brewery"),
            VF.createIRI("http://example.com/dotwebstack#Identifiable")))
        .build();

    assertThat(constraint.toString(), containsString(stringify(RDF.TYPE)));
    assertThat(constraint.toString(), containsString("http://example.com/dotwebstack#Brewery"));
    assertThat(constraint.toString(), containsString("http://example.com/dotwebstack#Identifiable"));
  }

  @Test
  public void toString_returnsCommaSeparatedString_forVariableConstraint() {
    OuterQuery<?> query = Queries.CONSTRUCT();

    Constraint constraint = Constraint.builder()
        .constraintType(ConstraintType.MINCOUNT)
        .predicate(() -> stringify(VF.createIRI("http://example.com/dotwebstack#hasBeer")))
        .values(Set.of(query.var()))
        .build();

    assertThat(constraint.toString(), is(equalTo("<http://example.com/dotwebstack#hasBeer>" + " ?x0")));
  }

}
