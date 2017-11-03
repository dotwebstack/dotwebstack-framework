package org.dotwebstack.framework.frontend.ld.writer.tuple;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultSerializer;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultXmlSerializer;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.TUPLE)
@Produces(MediaType.APPLICATION_XML)
public class XmlTupleEntityWriter extends AbstractJsonGeneratorTupleEntityWriter {

  XmlTupleEntityWriter() {
    super(MediaType.APPLICATION_XML_TYPE);
  }

  @Override
  protected JsonFactory createFactory() {
    return new XmlFactory();
  }

  @Override
  protected TupleQueryResultSerializer createSerializer() {
    return new TupleQueryResultXmlSerializer();
  }

}
