package org.dotwebstack.framework.backend.rdf4j.serializers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LocalDateSerializer implements Serializer {

  @Override
  public boolean supports(@NonNull Object object) {
    return object.getClass()
        .isAssignableFrom(LocalDate.class);
  }

  @Override
  public String serialize(@NonNull Object object) {
    return ((LocalDate) object).format(DateTimeFormatter.ISO_DATE);
  }
}
