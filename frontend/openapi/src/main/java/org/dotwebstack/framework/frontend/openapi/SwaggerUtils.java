package org.dotwebstack.framework.frontend.openapi;

import com.atlassian.oai.validator.interaction.ApiOperationResolver;
import com.atlassian.oai.validator.interaction.RequestValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.ApiOperationMatch;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.schema.SchemaValidator;
import io.swagger.models.Swagger;
import lombok.NonNull;

public final class SwaggerUtils {

  private SwaggerUtils() {}

  public static RequestValidator createValidator(@NonNull Swagger swagger) {
    LevelResolver levelResolver = LevelResolver.defaultResolver();
    MessageResolver messageResolver = new MessageResolver(levelResolver);
    SchemaValidator schemaValidator = new SchemaValidator(swagger, messageResolver);
    return new RequestValidator(schemaValidator, messageResolver, swagger);
  }

  public static ApiOperation extractApiOperation(@NonNull Swagger swagger, @NonNull String path,
      @NonNull String method) {
    Method realMethod = Method.valueOf(method.toUpperCase());

    ApiOperationMatch apiOperationMatch =
        new ApiOperationResolver(swagger, null).findApiOperation(path, realMethod);

    return apiOperationMatch.isPathFound() ? apiOperationMatch.getApiOperation() : null;
  }

}
