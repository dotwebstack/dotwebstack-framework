package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.frontend.http.MediaTypes;
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
  public void toResponse_ReturnsResponseBasedOnExceptionResponse_WhenCalled() throws Exception {
    // Arrange
    when(exception.getResponse()).thenReturn(Response.status(Status.NOT_ACCEPTABLE).build());
    when(exception.getMessage()).thenReturn("unacceptable!");

    // Act
    Response response = webApplicationExceptionMapper.toResponse(exception);

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.NOT_ACCEPTABLE));
    assertThat(response.getMediaType(), equalTo(MediaTypes.PROBLEM_JSON));
    ProblemDetails details = (ProblemDetails) response.getEntity();
    assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), details.getStatus());
    assertEquals(Status.NOT_ACCEPTABLE.getReasonPhrase(), details.getTitle());
    assertEquals("unacceptable!", details.getDetail());
    assertEquals(0, details.getExtendedDetails().size());
  }

  @Test
  public void toResponse_ReturnsResponseBasedOnRequestValidationException_WhenCalled()
      throws Exception {
    // Arrange
    Map<String, Object> detailsMap = new HashMap<>();
    detailsMap.put("detail1", "value1");
    detailsMap.put("detail2", "value2");
    ExtendedProblemDetailException x =
        new ExtendedProblemDetailException("message", Status.BAD_REQUEST, detailsMap);

    // Act
    Response response = webApplicationExceptionMapper.toResponse(x);

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.BAD_REQUEST));
    assertThat(response.getMediaType(), equalTo(MediaTypes.PROBLEM_JSON));
    ProblemDetails details = (ProblemDetails) response.getEntity();
    assertEquals(Status.BAD_REQUEST.getStatusCode(), details.getStatus());
    assertEquals(Status.BAD_REQUEST.getReasonPhrase(), details.getTitle());
    assertEquals("message", details.getDetail());
    assertEquals(2, details.getExtendedDetails().size());
  }

}
