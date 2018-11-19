package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.atlassian.oai.validator.model.ApiOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class RequestParameterExtractor {

  static final String PARAM_GEOMETRY_QUERYTYPE = "geometry_querytype";
  static final String PARAM_GEOMETRY = "geometry";
  static final String PARAM_PAGE_NUM = "page";
  static final String PARAM_PAGE_SIZE = "size";

  private final ObjectMapper objectMapper;

  RequestParameterExtractor(@NonNull ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  RequestParameters extract(@NonNull ApiOperation apiOperation,
      @NonNull ContainerRequestContext containerRequestContext) {

    UriInfo uriInfo = containerRequestContext.getUriInfo();

    RequestParameters parameters = new RequestParameters();

    parameters.putAll(uriInfo.getPathParameters());
    parameters.putAll(uriInfo.getQueryParameters());
    parameters.putAll(containerRequestContext.getHeaders());

    try {
      RequestBody requestBody = apiOperation.getOperation().getRequestBody();
      Optional<Schema> schema = Optional.empty();
      if (requestBody != null) {
        Content content = defaultIfNull(requestBody.getContent(), new Content());
        schema = content.values().stream() //
            .filter(mediaType -> "object".equalsIgnoreCase(mediaType.getSchema().getType())) //
            .findFirst() //
            .map(MediaType::getSchema);
      }
      extractBodyParameter(parameters, containerRequestContext, schema);
    } catch (IOException ioe) {
      throw new InternalServerErrorException("Error processing request body.", ioe);
    }

    LOG.info("Extracted parameters: {}", parameters);

    return parameters;
  }

  /**
   * Extracts the body from the supplied request.
   */
  private void extractBodyParameter(final RequestParameters requestParameters,
      final ContainerRequestContext ctx, final Optional<Schema> schema) throws IOException {

    String body = extractBody(ctx);
    if (body == null) {
      return;
    }
    requestParameters.setRawBody(body);
    if (!schema.isPresent()) {
      return;
    }

    if (ctx.getHeaders().get(HttpHeaders.CONTENT_TYPE).stream() //
        .map(String::toLowerCase) //
        .noneMatch(header -> header.startsWith(ContentType.APPLICATION_JSON.getMimeType()))) {
      return;
    }

    Map<String, Object> json = objectMapper.readValue(body, Map.class);

    for (Map.Entry<String, Object> entry : json.entrySet()) {
      requestParameters.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
    }
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

