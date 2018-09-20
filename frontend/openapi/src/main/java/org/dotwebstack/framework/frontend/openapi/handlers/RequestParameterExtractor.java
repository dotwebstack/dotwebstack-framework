package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
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

  void uriEncodeToList(Map.Entry<String, List<String>> entry) {
    entry.setValue(//
        entry.getValue() //
            .stream() //
            .map(s -> { //
              String encode = s;
              try {
                encode = URLEncoder.encode(s, ENCODING);
              } catch (UnsupportedEncodingException uee) {
                LOG.error("encoding failed {}", uee.getMessage());
              }
              return encode;
            }).collect(Collectors.toList()));
  }

  RequestParameters extract(@NonNull ApiOperation apiOperation, @NonNull Swagger swagger,
      @NonNull ContainerRequestContext containerRequestContext) {

    UriInfo uriInfo = containerRequestContext.getUriInfo();

    MultivaluedMap<String, String> headers = containerRequestContext.getHeaders();
    MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

    pathParameters.entrySet().forEach(this::uriEncodeToList);
    queryParameters.entrySet().forEach(this::uriEncodeToList);

    RequestParameters parameters = new RequestParameters();
    parameters.putAll(headers);
    parameters.putAll(pathParameters);
    parameters.putAll(queryParameters);

    boolean bodyParamDefined = apiOperation.getOperation().getParameters().stream() //
        .anyMatch(parameterBody -> isBodyParameter(swagger, parameterBody));
    boolean applicationHeaderSupplied = headers //
        .get(HttpHeaders.CONTENT_TYPE) //
        .stream() //
        .anyMatch(header -> ContentType.APPLICATION_JSON.toString().startsWith(header)); //

    appendBody(parameters, containerRequestContext, bodyParamDefined, applicationHeaderSupplied);

    LOG.info("Extracted parameters: {}", parameters);

    return parameters;
  }

  private boolean isBodyParameter(@NonNull Swagger swagger, Parameter parameter) {
    Optional<ModelImpl> parameterModel = getBodyParameterModel(swagger, parameter);
    return parameterModel.isPresent() //
        && "body".equalsIgnoreCase(parameter.getIn()) //
        && "object".equalsIgnoreCase(parameterModel.get().getType()); //
  }

  private static Optional<ModelImpl> getBodyParameterModel(@NonNull Swagger swagger,
      Parameter parameter) {
    if ((parameter instanceof BodyParameter)) {
      BodyParameter bodyParameter = (BodyParameter) parameter;
      if (bodyParameter.getSchema() instanceof ModelImpl) {
        return Optional.of((ModelImpl) bodyParameter.getSchema());
      }
      if (bodyParameter.getSchema() instanceof RefModel) {
        return Optional.of((ModelImpl) swagger.getDefinitions().get(
            ((RefModel) bodyParameter.getSchema()).getSimpleRef()));
      }
    }
    return Optional.empty();
  }

  private void appendBody(RequestParameters parameters,
      ContainerRequestContext containerRequestContext, boolean bodyParamDefined,
      boolean applicationHeaderSupplied) {
    try {
      parameters.setRawBody(extractBody(containerRequestContext));
      if (bodyParamDefined && applicationHeaderSupplied && parameters.getRawBody() != null) {
        Map<String, Object> json = objectMapper.readValue(parameters.getRawBody(), Map.class);
        for (Map.Entry<String, Object> entry : json.entrySet()) {
          parameters.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
        }
      }
    } catch (IOException ioe) {
      throw new InternalServerErrorException("Error processing request body.", ioe);
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

