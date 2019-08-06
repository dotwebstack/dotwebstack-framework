package org.dotwebstack.framework.backend.rdf4j.serializers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ZonedDateTimeSerializer implements Serializer {

  @Override
  public boolean supports(@NonNull Object object) {
    return object.getClass()
        .isAssignableFrom(ZonedDateTime.class);
  }

  @Override
  public String serialize(@NonNull Object object) {
    return ((ZonedDateTime) object).format(DateTimeFormatter.ISO_DATE_TIME);
  }
}
