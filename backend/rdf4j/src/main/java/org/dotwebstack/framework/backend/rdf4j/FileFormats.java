package org.dotwebstack.framework.backend.rdf4j;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.rio.RDFFormat;

public final class FileFormats {

  private static final Map<String, RDFFormat> FORMATS = Map.of("trig", RDFFormat.TRIG, "nq", RDFFormat.NQUADS);

  private FileFormats() {}

  public static RDFFormat getFormat(@NonNull String extension) {
    return FORMATS.get(extension.toLowerCase());
  }

}
