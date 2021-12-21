package org.dotwebstack.framework.service.openapi.jexl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;

@ExtendWith(MockitoExtension.class)
class RequestFunctionsTest {

  private RequestFunctions requestFunctions;

  @Mock
  private ServerRequest serverRequest;

  @Mock
  private ServerRequest.Headers headers;

  @BeforeEach
  void beforeEach() {
    requestFunctions = new RequestFunctions();
  }

  @Test
  void namespace_returnsCorrectly() {
    String namespace = requestFunctions.getNamespace();

    assertThat(namespace, is("req"));
  }

  @Test
  void accepts_givenMediaTypeAndMatchingHeaders_returnsTrue() {
    when(serverRequest.headers()).thenReturn(headers);
    when(headers.accept())
        .thenReturn(List.of(MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON));

    boolean accepts = requestFunctions.accepts("text/html", serverRequest);

    assertThat(accepts, is(true));
  }

  @Test
  void accepts_givenMediaTypeAndMatchingHeaders_returnsFalse() {
    when(serverRequest.headers()).thenReturn(headers);
    when(headers.accept()).thenReturn(List.of(MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON));

    boolean accepts = requestFunctions.accepts("text/html", serverRequest);

    assertThat(accepts, is(false));
  }

  @Test
  void accepts_givenMediaTypeAndNoAcceptHeaders_returnsFalse() {
    when(serverRequest.headers()).thenReturn(headers);
    when(headers.accept()).thenReturn(List.of());

    boolean accepts = requestFunctions.accepts("text/html", serverRequest);

    assertThat(accepts, is(false));
  }

  @Test
  void accepts_givenNullMediaType_throwsException() {
    assertThrows(NullPointerException.class, () -> requestFunctions.accepts(null, serverRequest));
  }

  @Test
  void accepts_givenNullServerRequest_throwsException() {
    assertThrows(NullPointerException.class, () -> requestFunctions.accepts(null, serverRequest));
  }
}
