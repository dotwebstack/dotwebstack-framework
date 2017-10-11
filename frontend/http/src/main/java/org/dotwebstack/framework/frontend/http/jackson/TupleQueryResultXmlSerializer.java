package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import java.io.IOException;
import javax.xml.namespace.QName;

public class TupleQueryResultXmlSerializer extends AbstractTupleQueryResultSerializer {

  private static final String XML_ROOT = "results";

  private static final String XML_ITEM = "result";

  @Override
  protected void writeStart(JsonGenerator jsonGenerator) throws IOException {
    ToXmlGenerator xmlGenerator = (ToXmlGenerator) jsonGenerator;
    xmlGenerator.setPrettyPrinter(new DefaultXmlPrettyPrinter());
    xmlGenerator.setNextName(new QName(null, XML_ROOT));
    xmlGenerator.writeStartObject();
  }

  @Override
  protected void writeStartItem(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeObjectFieldStart(XML_ITEM);
  }

  @Override
  protected void writeEnd(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeEndObject();
  }

  @Override
  protected void writeEndItem(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeEndObject();
  }
}
