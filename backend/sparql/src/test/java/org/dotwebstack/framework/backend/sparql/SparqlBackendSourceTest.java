package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SparqlBackendSourceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void builder() {
    // Act
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(DBEERPEDIA.BACKEND, "myQuery").build();

    // Assert
    assertThat(backendSource.getBackendReference(), equalTo(DBEERPEDIA.BACKEND));
    assertThat(backendSource.getQuery(), equalTo("myQuery"));
  }

  @Test
  public void requiredBackgroundReference() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(null, "myQuery").build();
  }

  @Test
  public void requiredQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(null, "myQuery").build();
  }
}
