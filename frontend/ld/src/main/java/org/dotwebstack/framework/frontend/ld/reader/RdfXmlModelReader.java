package org.dotwebstack.framework.frontend.ld.reader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.stereotype.Service;

@Service
@Consumes(MediaTypes.RDFXML)
public class RdfXmlModelReader implements MessageBodyReader<Model> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == Model.class;
  }

  @Override
  public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream)
      throws IOException {

    RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

    Model transactionModel = new LinkedHashModel();
    rdfParser.setRDFHandler(new StatementCollector(transactionModel));

    rdfParser.parse(inputStream, "");
    return transactionModel;
  }

}
