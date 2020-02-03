package org.dotwebstack.framework.backend.rdf4j.mapping;

import java.io.StringWriter;
import lombok.NonNull;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.util.MimeType;

public abstract class BaseResponseMapper implements ResponseMapper {

  abstract MimeType supportedMimeType();

  abstract RDFFormat rdfFormat();

  @Override
  public boolean supportsOutputMimeType(@NonNull MimeType mimeType) {
    return supportedMimeType().equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(@NonNull Class<?> clazz) {
    return Model.class == clazz;
  }

  @Override
  public String toResponse(@NonNull Object input) {
    if (input instanceof Model) {
      StringWriter sw = new StringWriter();
      Rio.write((Model) input, sw, rdfFormat());
      return sw.toString();
    } else {
      throw new IllegalArgumentException("Input can only be of the type Model.");
    }
  }
}
