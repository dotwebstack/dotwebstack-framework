package org.dotwebstack.framework.frontend.openapi.testutils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;

@Slf4j
public class ToOpenApi implements ArgumentConverter {
  @Override
  public OpenAPI convert(Object o, ParameterContext parameterContext)
      throws ArgumentConversionException {
    OpenAPI spec = new OpenAPI();
    if (o instanceof String) {
      InputStream resource = ClassLoader.getSystemClassLoader() //
          .getResourceAsStream("org/dotwebstack/framework/frontend/openapi/" + o);

      String result;
      try {
        result = CharStreams.toString(new InputStreamReader(resource, Charsets.UTF_8));
      } catch (IOException ioe) {
        LOG.error("Error Parsing OASpec. Are you sure the name is correct? error: {}", ioe);
        throw new ArgumentConversionException(ioe.toString());
      }
      ParseOptions options = new ParseOptions();
      options.setResolveFully(true);

      SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(result, null, options);
      parseResult.getMessages().forEach(System.out::print);
      spec = parseResult.getOpenAPI();
    }
    return spec;
  }


  @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  @ConvertWith(ToOpenApi.class)
  public @interface ToOpenApi3 {
  }
}