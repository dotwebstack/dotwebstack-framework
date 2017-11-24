package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebApplicationExceptionMapperTest {

  @Mock
  private WebApplicationException exception;

  private WebApplicationExceptionMapper webApplicationExceptionMapper;

  @Before
  public void setUp() {
    webApplicationExceptionMapper = new WebApplicationExceptionMapper();
  }

  @Test
  public void toResponse_ReturnsResponseBasedOnExceptionResponse_WhenCalled() {
    // Arrange
    when(exception.getResponse()).thenReturn(Response.status(Status.NOT_ACCEPTABLE).build());

    // Act
    Response response = webApplicationExceptionMapper.toResponse(exception);

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.NOT_ACCEPTABLE));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.getEntity().toString(), equalTo(Status.NOT_ACCEPTABLE.getReasonPhrase()));
  }

}
