package org.dotwebstack.framework.service.openapi.conversion;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(OpenApiProperties.class)
public class ZonedDateTimeTypeConverter implements TypeConverter<ZonedDateTime, String> {

  private OpenApiProperties openApiProperties;

  public ZonedDateTimeTypeConverter(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
  }

  @Override
  public boolean supports(Object object) {
    return object instanceof ZonedDateTime;
  }

  @Override
  public String convert(ZonedDateTime source, Map<String, Object> context) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    ZonedDateTime zonedDateTime = source;
    if (Objects.nonNull(openApiProperties.getDateproperties()) && Objects.nonNull(openApiProperties.getDateproperties()
        .getDateformat())) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(openApiProperties.getDateproperties()
          .getDatetimeformat());
    }

    if (Objects.nonNull(openApiProperties.getDateproperties()) && Objects.nonNull(openApiProperties.getDateproperties()
        .getTimezone())) {
      zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(openApiProperties.getDateproperties()
          .getTimezone()));
    }

    return dateTimeFormatter.format(zonedDateTime);
  }
}
