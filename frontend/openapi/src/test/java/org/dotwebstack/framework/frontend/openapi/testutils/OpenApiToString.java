package org.dotwebstack.framework.frontend.openapi.testutils;


import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
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
public class OpenApiToString implements ArgumentConverter {
  @Override
  public String convert(Object o, ParameterContext parameterContext)
      throws ArgumentConversionException {
    if (o instanceof String) {
      InputStream resource = ClassLoader.getSystemClassLoader().getResourceAsStream(
          "org/dotwebstack/framework/frontend/openapi/" + o);

      String result = null;
      try {
        result = CharStreams.toString(new InputStreamReader(resource, Charsets.UTF_8));
      } catch (IOException ioe) {
        log.error("Error Parsing OASpec. Are you sure the name is correct? error: {}", ioe);
      }
      return result;
    }
    return "";
  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  @ConvertWith(OpenApiToString.class)
  public @interface ToOpenApi3String {
  }

}
