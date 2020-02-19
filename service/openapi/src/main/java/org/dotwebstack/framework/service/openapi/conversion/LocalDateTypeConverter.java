package org.dotwebstack.framework.service.openapi.conversion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(OpenApiProperties.class)
public class LocalDateTypeConverter implements TypeConverter<LocalDate, String> {

  private OpenApiProperties openApiProperties;

  private DateTimeFormatter dateTimeFormatter;

  public LocalDateTypeConverter(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
    this.dateTimeFormatter = getDateTimeFormatter();
  }

  @Override
  public boolean supports(Object object) {
    return object instanceof LocalDate;
  }

  @Override
  public String convert(LocalDate source, Map<String, Object> context) {
    return dateTimeFormatter.format(source);
  }

  private DateTimeFormatter getDateTimeFormatter() {
    if (Objects.nonNull(openApiProperties.getDateproperties()) && Objects.nonNull(openApiProperties.getDateproperties()
        .getDateformat())) {
      return DateTimeFormatter.ofPattern(openApiProperties.getDateproperties()
          .getDateformat());
    }
    return DateTimeFormatter.ISO_LOCAL_DATE;
  }
}
