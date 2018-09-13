package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.atlassian.oai.validator.model.ApiOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class RequestParameterExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParameterExtractor.class);
  private static final String ENCODING = "UTF-8";

  static final String PARAM_PAGE_NUM = "page";
  static final String PARAM_PAGE_SIZE = "size";

  private final ObjectMapper objectMapper;

  RequestParameterExtractor(@NonNull ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  RequestParameters extract(@NonNull ApiOperation apiOperation, @NonNull Swagger swagger,
                            @NonNull ContainerRequestContext containerRequestContext) {

    boolean properBodyParamExists = //
        apiOperation //
            .getOperation() //
            .getParameters() //
            .stream() //
            .anyMatch(isBodyParameter(swagger)) //
            && containerRequestContext //
            .getHeaders() //
            .get(HttpHeaders.CONTENT_TYPE) //
            .stream() //
            .anyMatch(header -> ContentType.APPLICATION_JSON.toString().startsWith(header)) //
            && containerRequestContext.hasEntity(); //

    try {
      return getParameters(containerRequestContext, properBodyParamExists);
    } catch (IOException ioe) {
      throw new InternalServerErrorException("Error processing request body.", ioe);
    }
  }

  private Predicate<Parameter> isBodyParameter(final Swagger swagger) {
    return parameterBody -> {

      if (parameterBody instanceof BodyParameter
          && parameterBody.getIn().equalsIgnoreCase("body")) {

        Model bodyModel = ((BodyParameter) parameterBody).getSchema();

        if (bodyModel instanceof ModelImpl) {
          return ((ModelImpl) bodyModel).getType().equalsIgnoreCase("object");
        }

        if (bodyModel instanceof RefModel) {
          return ((ModelImpl) swagger.getDefinitions().get(
              ((RefModel) bodyModel).getSimpleRef())).getType().equalsIgnoreCase("object");
        }
      }
      return false;
    };
  }

  private RequestParameters getParameters(ContainerRequestContext containerRequestContext,
                                          boolean properBodyParamExists) throws IOException {

    RequestParameters parameters = new RequestParameters();
    String body = extractBody(containerRequestContext);
    parameters.setRawBody(body);
    UriInfo uriInfo = containerRequestContext.getUriInfo();

    MultivaluedMap<String, String> headers = containerRequestContext.getHeaders();
    MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    MultivaluedMap<String, String> bodyParameters =
        !isBlank(body) && properBodyParamExists ? getBodyParameters(body)
            : new MultivaluedHashMap<>();

    pathParameters.entrySet().forEach(this::uriEncodeToList);
    queryParameters.entrySet().forEach(this::uriEncodeToList);

    parameters.putAll(headers);
    parameters.putAll(queryParameters);
    parameters.putAll(pathParameters);
    parameters.putAll(bodyParameters);

    LOG.info("Extracted parameters: {}", parameters);
    return parameters;
  }

  private MultivaluedMap<String, String> getBodyParameters(String rawBody) throws IOException {
    MultivaluedMap<String, String> bodyParameters = new MultivaluedHashMap<>();
    Map<String, Object> json = objectMapper.readValue(rawBody, Map.class);
    for (Map.Entry<String, Object> entry : json.entrySet()) {
      bodyParameters.add(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
    }
    return bodyParameters;
  }

  void uriEncodeToList(Map.Entry<String, List<String>> entry) {
    entry.setValue( //
        entry.getValue() //
            .stream() //
            .map(this::urlEncode) //
            .collect(Collectors.toList()));
  }

  private String urlEncode(String url) {
    try {
      return URLEncoder.encode(url, ENCODING);
    } catch (UnsupportedEncodingException uee) {
      LOG.error("encoding failed {}", uee.getMessage());
    }
    return url;
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

    if (!ctx.hasEntity()) {
      return null;
    }

    /*
     * do not use auto-closable construction as it is responsibility of JAX-RS to close the
     * underlying stream
     * still copy from PushbackStream, not from the underlying stream
     */
    StringWriter writer = new StringWriter();
    IOUtils.copy(new PushbackInputStream(ctx.getEntityStream()), writer);
    return writer.toString();
  }
}
