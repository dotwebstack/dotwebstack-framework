package org.dotwebstack.framework.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

@Data
public class Query {

  @NotBlank
  private String type;

  private List<String> keys = new ArrayList<>();

  private boolean list = false;

  private boolean pageable = false;

  private boolean batch = false;

  private String context;

  private Map<String, List<SortableByConfiguration>> sortableBy = new HashMap<>();
}
