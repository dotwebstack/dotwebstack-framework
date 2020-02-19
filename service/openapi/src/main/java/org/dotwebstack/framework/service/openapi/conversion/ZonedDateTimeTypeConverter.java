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

  private DateTimeFormatter dateTimeFormatter;

  private ZoneId zoneId;

  public ZonedDateTimeTypeConverter(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
    this.dateTimeFormatter = getDateTimeFormatter();
    this.zoneId = getZoneId();
  }

  @Override
  public boolean supports(Object object) {
    return object instanceof ZonedDateTime;
  }

  @Override
  public String convert(ZonedDateTime source, Map<String, Object> context) {
    return dateTimeFormatter.format(convertDate(source));
  }

  private DateTimeFormatter getDateTimeFormatter() {
    if (Objects.nonNull(openApiProperties.getDateproperties()) && Objects.nonNull(openApiProperties.getDateproperties()
        .getDatetimeformat())) {
      return DateTimeFormatter.ofPattern(openApiProperties.getDateproperties()
          .getDatetimeformat());
    }

    return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  }

  private ZoneId getZoneId() {
    if (Objects.nonNull(openApiProperties.getDateproperties()) && Objects.nonNull(openApiProperties.getDateproperties()
        .getTimezone())) {
      return ZoneId.of(openApiProperties.getDateproperties()
          .getTimezone());
    }
    return null;
  }

  private ZonedDateTime convertDate(ZonedDateTime source) {
    return Objects.nonNull(zoneId) ? source.withZoneSameInstant(zoneId) : source;
  }
}
