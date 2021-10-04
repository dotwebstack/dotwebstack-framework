package org.dotwebstack.framework.backend.rdf4j.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.junit.jupiter.api.Test;

class GraphPatternWithValuesTest {

  private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Test
  void getQueryString_containsValueBlock_whenGiven() {
    var typeVar = SparqlBuilder.var("x1");

    var graphPattern = new GraphPatternWithValues(
        GraphPatterns.tp(VALUE_FACTORY.createIRI("https://github.com/dotwebstack/beer/def#Beer"), RDF.TYPE, typeVar),
        Map.of(typeVar, Set.of(OWL.CLASS)));

    var queryString = graphPattern.getQueryString();

    assertThat(queryString, equalTo("VALUES ?x1 {<http://www.w3.org/2002/07/owl#Class>}" + System.lineSeparator()
        + "<https://github.com/dotwebstack/beer/def#Beer> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x1 ."));
  }
}
