package org.dotwebstack.framework.service.openapi.jexl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.stereotype.Component;

@Component
public class DateFunction implements JexlFunction {

  private static final String NAMESPACE = "date";

  @Override
  public String getNamespace() {
    return NAMESPACE;
  }

  @SuppressWarnings("unused")
  public String currentDate() {
    return LocalDateTime.now()
        .format(DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
