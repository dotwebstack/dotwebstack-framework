package org.dotwebstack.framework.frontend.openapi.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

final class RequestParameterExtractor {

  static final String PARAM_GEOMETRY_QUERYTYPE = "geometry_querytype";
  static final String PARAM_GEOMETRY = "geometry";
  static final String RAW_REQUEST_BODY = "raw-request-body";
  static final String PARAM_PAGE_NUM = "page";
  static final String PARAM_PAGE_SIZE = "size";

  private RequestParameterExtractor() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", RequestParameterExtractor.class));
  }

  static RequestParameters extract(@NonNull ContainerRequestContext containerRequestContext,
      @NonNull ObjectMapper objectMapper) {

    UriInfo uriInfo = containerRequestContext.getUriInfo();

    RequestParameters parameters = new RequestParameters();

    parameters.putAll(uriInfo.getPathParameters());
    parameters.putAll(uriInfo.getQueryParameters());
    parameters.putAll(containerRequestContext.getHeaders());

    try {
      parameters.putAll(extractBodyParameter(containerRequestContext, objectMapper));
    } catch (IOException ioe) {
      throw new InternalServerErrorException("Error processing request body.", ioe);
    }
    return parameters;
  }

  /**
   * Extracts the body from the supplied request.
   */
  private static Map<String, Object> extractBodyParameter(ContainerRequestContext ctx,
      ObjectMapper objectMapper) throws IOException {

    String body = extractBody(ctx);
    if (body == null) {
      return ImmutableMap.of();
    }

    ImmutableMap.Builder<String, Object> builder = new Builder<>();
    builder.put(RAW_REQUEST_BODY, body);

    return builder.build();
  }

  /**
   * Extracts body from a provided request context. Note that this method blocks until the body is
   * not fully read. Also note that it would not be possible to read from the underlying stream
   * again. {@link IOUtils#copy(InputStream, Writer)} is used.
   *
   * @param ctx JAX-RS request context
   * @return body of the underlying stream or <code>null</code> if the provided request contains no
   *         body
   * @throws IOException in case of any problems with the underlying stream
   */
  @SuppressWarnings("resource")
  @Nullable
  private static String extractBody(ContainerRequestContext ctx) throws IOException {
    /*
     * do not use auto-closable construction as it is responsibility of JAX-RS to close the
     * underlying stream
     */
    IsEmptyCheckInputStream inputStream = new IsEmptyCheckInputStream(ctx.getEntityStream());

    if (inputStream.isEmpty()) {
      return null;
    }

    StringWriter writer = new StringWriter();
    /* still copy from PushbackStream, not from the underlying stream */
    IOUtils.copy(inputStream, writer);
    return writer.toString();
  }

  private static final class IsEmptyCheckInputStream extends PushbackInputStream {

    private IsEmptyCheckInputStream(InputStream input) {
      super(input);
    }

    private boolean isEmpty() throws IOException {
      byte[] buf = new byte[1];
      int result = read(buf, 0, 1);

      if (result > 0) {
        unread(buf[0]);
        return false;
      }

      return true;
    }

  }
}

