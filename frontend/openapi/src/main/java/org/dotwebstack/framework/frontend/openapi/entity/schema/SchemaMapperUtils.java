package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

final class SchemaMapperUtils {

  private SchemaMapperUtils() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", SchemaMapperUtils.class));
  }

  public static Literal castLiteralValue(Value value) {
    if (!(value instanceof Literal)) {
      throw new SchemaMapperRuntimeException("Value is not a literal value.");
    }

    return ((Literal) value);
  }

  public static Map<String, String> createLink(URI href) {
    return ImmutableMap.of("href", href.toString());
  }

}
