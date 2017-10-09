package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.eclipse.rdf4j.query.TupleQueryResult;

public interface TupleQueryResultSerializer {

  void serialize(TupleQueryResult tupleQueryResult, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException;
}
