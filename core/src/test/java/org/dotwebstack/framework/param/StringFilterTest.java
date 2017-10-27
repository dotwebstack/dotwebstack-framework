package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

public class StringFilterTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private Parameter parameter;

  private IRI id = VALUE_FACTORY.createIRI("http://foo#", "bar");

  @Before
  public void setUp() {
    parameter = new StringFilter(id, "variable");
  }

  @Test
  public void handle_ModifiesQuery_WithSingleVariable() {
    parameter = new StringFilter(id, "variable");
    String result = parameter.handle("value", "SELECT ${variable}");

    assertThat(result, is("SELECT value"));
  }

  @Test
  public void handle_ModifiesQuery_WithDotsInVariable() {
    parameter = new StringFilter(id, "vari.able");
    String result = parameter.handle("value", "SELECT ${vari.able}");

    assertThat(result, is("SELECT value"));
  }

  @Test
  public void handle_ModifiesQuery_WithMultipleVariables() {
    String result = parameter.handle("value", "SELECT ${variable} WHERE ${variable}");

    assertThat(result, is("SELECT value WHERE value"));
  }

  @Test
  public void handle_ModifiesQuery_WithNullVariable() {
    String result = parameter.handle("null", "SELECT ${variable}");

    assertThat(result, is("SELECT null"));
  }

  @Test
  public void handle_DoesNotModifyQuery_WithoutVariable() {
    String result = parameter.handle("value", "SELECT ?noVariable");

    assertThat(result, is("SELECT ?noVariable"));
  }

  @Test
  public void handle_DoesNotModifyQuery_WithoutMatchingVariable() {
    String result = parameter.handle("value", "SELECT ${noMatchingVariable}");

    assertThat(result, is("SELECT ${noMatchingVariable}"));
  }

}
