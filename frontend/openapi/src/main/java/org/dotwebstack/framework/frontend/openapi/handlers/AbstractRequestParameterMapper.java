package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
abstract class AbstractRequestParameterMapper {

  protected Map<String, String> getBodyParameters(@NonNull Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters, RequestBody requestBody) {

    return Stream.of(requestBody) //
        .filter(Objects::nonNull) //
        .map(RequestBody::getContent) //
        .filter(Objects::nonNull) //
        .map(content -> content.get(MediaType.APPLICATION_JSON.toString())) //
        .filter(Objects::nonNull) //
        .map(io.swagger.v3.oas.models.media.MediaType::getSchema) //
        .filter(Objects::nonNull) //
        .map(Schema::getProperties) //
        .filter(Objects::nonNull) //
        .map(Map::values) //
        .flatMap(Collection::stream) //
        .map(obj -> (Schema) obj) //
        .map(Schema::getExtensions) //
        .filter(Objects::nonNull) //
        .map(extensions -> (String) extensions.get(OpenApiSpecificationExtensions.PARAMETER)) //
        .filter(Objects::nonNull) //
        .map(id -> ParameterUtils.getParameter(parameters, id)) //
        .map(Parameter::getName) //
        .collect(Collectors.toMap(name -> name, requestParameters::get));

  }

  protected Map<String, String> getOtherParameters(Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters,
      io.swagger.v3.oas.models.parameters.Parameter openApiParameter) {

    return Stream.of(openApiParameter) //
        .map(io.swagger.v3.oas.models.parameters.Parameter::getExtensions) //
        .filter(Objects::nonNull) //
        .map(extensions -> (String) extensions.get(OpenApiSpecificationExtensions.PARAMETER)) //
        .filter(Objects::nonNull) //
        .map(id -> ParameterUtils.getParameter(parameters, id)) //
        .map(Parameter::getName) //
        .collect(Collectors.toMap(name -> name, requestParameters::get));
  }

}

