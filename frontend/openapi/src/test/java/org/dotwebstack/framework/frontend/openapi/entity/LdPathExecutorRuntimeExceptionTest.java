package org.dotwebstack.framework.frontend.openapi.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class LdPathExecutorRuntimeExceptionTest {

  @Test
  public void testConstructorMessageException() {
    Exception exceptionMock = mock(Exception.class);
    LdPathExecutorRuntimeException executorRuntimeException =
        new LdPathExecutorRuntimeException("message", exceptionMock);
    assertEquals("message", executorRuntimeException.getMessage());
  }

  @Test
  public void testConstructorMessage() {
    LdPathExecutorRuntimeException executorRuntimeException =
        new LdPathExecutorRuntimeException("message");
    assertEquals("message", executorRuntimeException.getMessage());
  }
}
