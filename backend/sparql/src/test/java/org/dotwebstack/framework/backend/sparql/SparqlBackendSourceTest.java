package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.Backend;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendSourceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Backend backend;

  @Test
  public void builder() {
    // Act
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, "myQuery").build();

    // Assert
    assertThat(backendSource.getBackend(), equalTo(backend));
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
