package org.dotwebstack.framework.frontend.http.provider.tuple;

import com.fasterxml.jackson.core.JsonFactory;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultJsonSerializer;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultSerializer;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.TUPLE)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMessageBodyWriter extends AbstractJsonGeneratorMessageBodyWriter {

  JsonMessageBodyWriter() {
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
