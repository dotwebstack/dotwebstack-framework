package org.dotwebstack.framework.frontend.ld.writer.tuple;

import com.fasterxml.jackson.core.JsonFactory;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultJsonSerializer;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultSerializer;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.TUPLE)
@Produces(MediaType.APPLICATION_JSON)
public class JsonTupleEntityWriter extends AbstractJsonGeneratorTupleEntityWriter {

  JsonTupleEntityWriter() {
    super(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  protected JsonFactory createFactory() {
    return new JsonFactory();
  }

  @Override
  protected TupleQueryResultSerializer createSerializer() {
    return new TupleQueryResultJsonSerializer();
  }

}
