package org.dotwebstack.framework.config;

import com.google.common.collect.ImmutableMap;
import org.eclipse.rdf4j.rio.RDFFormat;

public final class ConfigFileFormats {

  private static ImmutableMap<String, RDFFormat> formats =
      ImmutableMap.of("ttl", RDFFormat.TURTLE, "xml", RDFFormat.RDFXML, "json", RDFFormat.JSONLD);

  private ConfigFileFormats() {
    throw new UnsupportedOperationException(
        String.format("%s is not meant to be instantiated.", ConfigProperties.class.getName()));
  }

  public static boolean containsExtension(String extension) {
    return formats.containsKey(extension);
  }

  public static RDFFormat getFormat(String extension) {
    return formats.get(extension);
  }

}
