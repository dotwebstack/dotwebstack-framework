package org.dotwebstack.framework.backend.rdf4j.mapping;

import java.io.StringWriter;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.util.MimeType;

public abstract class BaseResponseMapper implements ResponseMapper<Model> {

  abstract MimeType supportedMimeType();

  abstract RDFFormat rdfFormat();

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    return supportedMimeType().equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(Class clazz) {
    return Model.class == clazz;
  }

  @Override
  public String toResponse(Model model) {
    StringWriter sw = new StringWriter();
    Rio.write(model, sw, rdfFormat());
    return sw.toString();
  }
}
