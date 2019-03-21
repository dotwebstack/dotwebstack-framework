package org.dotwebstack.framework.backend.rdf4j;

import com.google.common.collect.ImmutableMap;
import org.eclipse.rdf4j.rio.RDFFormat;

final class FileFormats {

  private static final ImmutableMap<String, RDFFormat> FORMATS =
      ImmutableMap.of("trig", RDFFormat.TRIG, "nq", RDFFormat.NQUADS);

  private FileFormats() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", FileFormats.class));
  }

  static RDFFormat getFormat(String extension) {
    return FORMATS.get(extension);
  }

}
