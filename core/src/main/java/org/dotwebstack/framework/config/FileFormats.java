package org.dotwebstack.framework.config;

import com.google.common.collect.ImmutableMap;
import org.eclipse.rdf4j.rio.RDFFormat;

public final class FileFormats {

  private static final ImmutableMap<String, RDFFormat> FORMATS =
      ImmutableMap.of("trig", RDFFormat.TRIG, "nq", RDFFormat.NQUADS, "json", RDFFormat.JSONLD);

  private FileFormats() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", FileFormats.class));
  }

  public static boolean containsExtension(String extension) {
    return FORMATS.containsKey(extension);
  }

  public static RDFFormat getFormat(String extension) {
    return FORMATS.get(extension);
  }

}
