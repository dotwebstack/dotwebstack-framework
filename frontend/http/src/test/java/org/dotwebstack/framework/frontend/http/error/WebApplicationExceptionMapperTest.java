package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException.InvalidParameter;
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
    assertEquals(0, details.getInvalidParameters().size());
  }

  @Test
  public void toResponse_ReturnsResponseBasedOnRequestValidationException_WhenCalled()
      throws Exception {
    // Arrange
    Map<String, String> detailsMap = new HashMap<>();
    detailsMap.put("detail1", "value1");
    detailsMap.put("detail2", "value2");
    List<InvalidParamsBadRequestException.InvalidParameter> details = new ArrayList<>();
    details.add(new InvalidParameter("detail1", "value1"));
    details.add(new InvalidParameter("detail2", "value2"));
    InvalidParamsBadRequestException x = new InvalidParamsBadRequestException("message", details);

    // Act
    Response response = webApplicationExceptionMapper.toResponse(x);

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.BAD_REQUEST));
    assertThat(response.getMediaType(), equalTo(MediaTypes.PROBLEM_JSON));
    ProblemDetails problemdetails = (ProblemDetails) response.getEntity();
    assertEquals(Status.BAD_REQUEST.getStatusCode(), problemdetails.getStatus());
    assertEquals(Status.BAD_REQUEST.getReasonPhrase(), problemdetails.getTitle());
    assertEquals("message", problemdetails.getDetail());
    assertEquals(2, problemdetails.getInvalidParameters().size());
  }

}
