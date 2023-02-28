package org.dotwebstack.framework.templating.pebble.extension;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.templating.pebble.filter.JsonLdFilter;
import org.springframework.stereotype.Component;

@Component
public class PebbleRdf4jExtension extends AbstractExtension {

  private static final String JSON_LD_FILTER = "jsonld";

  private final JsonLdFilter jsonLdFilter;

  public PebbleRdf4jExtension(JsonLdFilter jsonLdFilter) {
    this.jsonLdFilter = jsonLdFilter;
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, Filter> filters = new HashMap<>();
    filters.put(JSON_LD_FILTER, jsonLdFilter);
    return filters;
  }
}
