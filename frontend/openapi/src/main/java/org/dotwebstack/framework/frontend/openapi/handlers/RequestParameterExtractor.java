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

  // private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private RequestParameterExtractor() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", RequestParameterExtractor.class));
  }

  static RequestParameters extract(@NonNull ContainerRequestContext containerRequestContext,
      ObjectMapper mapper) {

    UriInfo uriInfo = containerRequestContext.getUriInfo();

    RequestParameters parameters = new RequestParameters();

    parameters.putAll(uriInfo.getPathParameters());
    parameters.putAll(uriInfo.getQueryParameters());
    parameters.putAll(containerRequestContext.getHeaders());

    try {
      parameters.putAll(extractBodyParameter(containerRequestContext, parameters, mapper));
    } catch (IOException ioe) {
      throw new InternalServerErrorException("Error processing request body.", ioe);
    }
    return parameters;
  }

  /**
   * Extracts the body from the supplied request. The body must either be empty or have the
   * following JSON structure:
   *
   * <pre>
   * _geo: {
   *   contains: {
   *     type: Point,
   *     coordinates: [123.32, 1234.23]
   *   }
   * }
   * </pre>
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Object> extractBodyParameter(ContainerRequestContext ctx,
      RequestParameters parameters, ObjectMapper mapper) throws IOException {

    String body = extractBody(ctx);
    if (body == null) {
      return ImmutableMap.of();
    }

    // JSON is validated by the RequestParameterValidator
    // Therefore, we can safely assume the JSON has the required structure (see Javadoc)

    ImmutableMap.Builder<String, Object> builder = new Builder<>();
    builder.put(RAW_REQUEST_BODY, body);

    // The extractGeometry method uses classes defined in grid-commons. Since we can't use that as
    // a dependency, the method is not usable at the moment.

    // Map<String, Object> json = mapper.readValue(body, Map.class);
    // Map<String, Object> query = (Map<String, Object>) json.get("_geo");
    //
    // if (query != null) {
    // String queryType = query.keySet().iterator().next();
    //
    // builder.put(PARAM_GEOMETRY_QUERYTYPE, queryType);
    //
    // Object geoJsonObject = query.get(queryType);
    // Geometry geometry = extractGeometry(geoJsonObject, parameters);
    //
    // builder.put(PARAM_GEOMETRY, null);
    // }

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


  // private static Geometry extractGeometry(Object geoJsonObject, RequestParameters parameters)
  // throws JsonProcessingException {
  // String geoJsonString = OBJECT_MAPPER.writeValueAsString(geoJsonObject);
  // GeoJSONReader reader = new GeoJSONReader();
  // Geometry geometry = reader.read(geoJsonString);
  //
  // String contentCrs = parameters.asString(RestConfiguration.PARAM_CONTENT_CRS);
  //
  // if (contentCrs != null) {
  // Projector projector = new Projector();
  //
  // return projector.project(geometry, Crs.valueOfEpsgCode(contentCrs), Crs.ETRS89);
  // }
  //
  // return geometry;
  // }

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

