package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendTest {

  @Test
  public void builder() {
    // Act
    SparqlBackend backend =
        new SparqlBackend.Builder(DBEERPEDIA.BACKEND, DBEERPEDIA.ENDPOINT.stringValue()).build();

    // Assert
    assertThat(backend.getIdentifier(), equalTo(DBEERPEDIA.BACKEND));
    assertThat(backend.getEndpoint(), equalTo(DBEERPEDIA.ENDPOINT.stringValue()));
  }

}
