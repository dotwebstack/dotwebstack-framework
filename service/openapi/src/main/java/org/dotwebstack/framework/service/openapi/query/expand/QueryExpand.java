package org.dotwebstack.framework.service.openapi.query.expand;

import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;

public class QueryExpand {
  private final Set<String> expandable;

  private final Set<String> expanded;

  private final String[] entries;

  private boolean rootFound;


  public QueryExpand(Set<String> expandable, Set<String> expanded) {
    this(expandable, expanded, new String[] {}, false);
  }

  public QueryExpand(Set<String> expandable, Set<String> expanded, String[] entries, boolean rootFound) {
    this.expandable = expandable;
    this.expanded = expanded;
    this.entries = entries;
    this.rootFound = rootFound;
  }

  public QueryExpand appendField(String key, Schema<?> schema) {
    if (!isEnvelope(schema) && rootFound) {
      var newEntries = new String[entries.length + 1];
      System.arraycopy(entries, 0, newEntries, 0, entries.length);
      newEntries[newEntries.length - 1] = key;
      return new QueryExpand(expandable, expanded, newEntries, rootFound);
    }
    rootFound = rootFound || (!isEnvelope(schema));
    return this;
  }

  public void appendSchema(Schema<?> schema) {
    rootFound = rootFound || (!isEnvelope(schema));
  }

  public String toString() {
    return String.join(".", entries);
  }

  public boolean expanded() {
    var pathString = this.toString();
    return !expandable.contains(pathString) || expanded.contains(pathString);
  }

  public boolean isExpandable() {
    return expandable.contains(this.toString());
  }
}
