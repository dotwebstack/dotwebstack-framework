package org.dotwebstack.framework.frontend.http.provider.tuple;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.TUPLE)
@Produces(MediaType.APPLICATION_XML)
public class XmlMessageBodyWriter extends AbstractJsonGeneratorMessageBodyWriter {

  XmlMessageBodyWriter() {
    super(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  protected JsonFactory createFactory() {
    return new XmlFactory();
  }

}
