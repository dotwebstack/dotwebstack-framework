package org.dotwebstack.framework.backend.rdf4j.mapping;

import java.io.StringWriter;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.util.MimeType;

public class Notation3ResponseMapper implements ResponseMapper<Model> {

  private static final MimeType N3_MEDIA_TYPE = MimeType.valueOf("text/n3");

  @Override
  public boolean supportsOutputMimeType(MimeType mediaType) {
    return N3_MEDIA_TYPE.equals(mediaType);
  }

  @Override
  public boolean supportsInputObjectClass(Class clazz) {
    return Model.class == clazz;
  }

  @Override
  public String toResponse(Model model) {
    StringWriter sw = new StringWriter();
    Rio.write(model, sw, RDFFormat.N3);
    return sw.toString();
  }
}
