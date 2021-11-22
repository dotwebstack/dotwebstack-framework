package org.dotwebstack.framework.service.openapi.query.mapping;

import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;

public class MappingContext {
  private final Set<String> expandablePaths;

  private final Set<String> expandedPaths;

  private final String[] path;

  private boolean rootFound;


  public MappingContext(Set<String> expandablePaths, Set<String> expandedPaths) {
    this(expandablePaths, expandedPaths, new String[] {}, false);
  }

  public MappingContext(Set<String> expandablePaths, Set<String> expandedPaths, String[] path, boolean rootFound) {
    this.expandablePaths = expandablePaths;
    this.expandedPaths = expandedPaths;
    this.path = path;
    this.rootFound = rootFound;
  }

  public MappingContext updatePath(String key, Schema<?> schema) {
    if (!isEnvelope(schema) && rootFound) {
      String[] newPath = createNewPath(key);
      return new MappingContext(expandablePaths, expandedPaths, newPath, rootFound);
    }
    rootFound = rootFound || (!isEnvelope(schema));
    return this;
  }

  public MappingContext updatePath(Schema<?> schema) {
    return new MappingContext(expandablePaths, expandedPaths, createNewPath(), rootFound || (!isEnvelope(schema)));
  }

  public String toString() {
    return String.join(".", path);
  }

  public boolean expanded() {
    var pathString = this.toString();
    return !expandablePaths.contains(pathString) || expandedPaths.contains(pathString);
  }

  public boolean isExpandable() {
    return expandablePaths.contains(this.toString());
  }

  private String[] createNewPath(String key) {
    var newPath = new String[path.length + 1];
    System.arraycopy(path, 0, newPath, 0, path.length);
    newPath[newPath.length - 1] = key;
    return newPath;
  }

  private String[] createNewPath() {
    var newPath = new String[path.length];
    System.arraycopy(path, 0, newPath, 0, newPath.length);
    return newPath;
  }
}
