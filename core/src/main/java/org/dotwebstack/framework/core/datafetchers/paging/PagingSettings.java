package org.dotwebstack.framework.core.datafetchers.paging;

import lombok.Data;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.stereotype.Component;

@Data
@Component
public class PagingSettings {

  private final Schema schema;

  private final int firstMaxValue;

  private final int offsetMaxValue;

  public PagingSettings(Schema schema) {
    this.schema = schema;
    this.firstMaxValue = schema.getSettings()
        .getFirstMaxValue();
    this.offsetMaxValue = schema.getSettings()
        .getOffsetMaxValue();
  }
}
