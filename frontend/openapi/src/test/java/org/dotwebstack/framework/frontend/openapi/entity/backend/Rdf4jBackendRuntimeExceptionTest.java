package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.Test;

// XXX (PvH) Sonar pleasing?
public class Rdf4jBackendRuntimeExceptionTest {
  @Test
  public void testConstructorMessageException() {
    RepositoryException exceptionMock = mock(RepositoryException.class);
    Rdf4jBackendRuntimeException executorRuntimeException =
        new Rdf4jBackendRuntimeException("message", exceptionMock);
    assertEquals("message", executorRuntimeException.getMessage());
  }

}
