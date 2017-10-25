package org.dotwebstack.framework.filter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

public class StringFilterTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private Filter filter;

  @Before
  public void setUp() {
    IRI id = VALUE_FACTORY.createIRI("http://foo#", "bar");

    filter = new StringFilter(id, "variable");
  }

  @Test
  public void filter_ModifiesQuery_WithSingleVariable() {
    String result = filter.filter("value", "SELECT ${variable}");

    assertThat(result, is("SELECT value"));
  }

  @Test
  public void filter_ModifiesQuery_WithMultipleVariables() {
    String result = filter.filter("value", "SELECT ${variable} WHERE ${variable}");

    assertThat(result, is("SELECT value WHERE value"));
  }

  @Test
  public void filter_ModifiesQuery_WithNullVariable() {
    String result = filter.filter("null", "SELECT ${variable}");

    assertThat(result, is("SELECT null"));
  }

  @Test
  public void filter_DoesNotModifyQuery_WithoutVariable() {
    String result = filter.filter("value", "SELECT ?noVariable");

    assertThat(result, is("SELECT ?noVariable"));
  }

  @Test
  public void filter_DoesNotModifyQuery_WithoutMatchingVariable() {
    String result = filter.filter("value", "SELECT ${noMatchingVariable}");

    assertThat(result, is("SELECT ${noMatchingVariable}"));
  }

}
