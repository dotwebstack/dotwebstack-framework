package org.dotwebstack.framework.backend.rdf4j;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.rio.RDFFormat;

@UtilityClass
final class FileFormats {

  private static final ImmutableMap<String, RDFFormat> FORMATS =
      ImmutableMap.of("trig", RDFFormat.TRIG, "nq", RDFFormat.NQUADS);

  static RDFFormat getFormat(@NonNull String extension) {
    return FORMATS.get(extension.toLowerCase());
  }

}
