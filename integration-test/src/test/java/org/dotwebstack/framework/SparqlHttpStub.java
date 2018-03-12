package org.dotwebstack.framework;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.NotSupportedException;
import org.apache.http.HttpStatus;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.impl.MutableTupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

@SuppressWarnings("restriction")
public class SparqlHttpStub {

  private static SparqlHttpStub instance;

  private HttpServer server;

  private Model graphResult;

  private TupleQueryResultBuilder tupleResultBuilder;

  private int responseCode = 0;

  private SparqlHttpStub() {
    init();
  }

  public static void start() {
    if (instance == null) {
      instance = new SparqlHttpStub();
      instance.startServer();
    }
  }

  public static void stop() {
    instance.stopServer();
    instance = null;
  }

  public static void returnGraph(Model model) {
    assertThat(instance, is(not(nullValue())));
    instance.tupleResultBuilder = null;
    instance.graphResult = model;
    instance.responseCode = HttpStatus.SC_OK;
  }

  public static void returnTuple(TupleQueryResultBuilder builder) {
    assertThat(instance, is(not(nullValue())));
    instance.tupleResultBuilder = builder;
    instance.graphResult = null;
    instance.responseCode = HttpStatus.SC_OK;
  }

  public static void setResponseCode(int responseCode) {
    assertThat(instance, is(not(nullValue())));
    instance.tupleResultBuilder = null;
    instance.graphResult = null;
    instance.responseCode = responseCode;
  }

  public static void main(String[] args) throws IOException {
    SparqlHttpStub.start();

    // returnGraphExample();
    returnTupleExample();

    System.out.println("Press enter to exit.");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    br.readLine();

    SparqlHttpStub.stop();
  }

  private static void returnTupleExample() {
    TupleQueryResultBuilder builder =
        new TupleQueryResultBuilder("name", "fte", "year").resultSet(DBEERPEDIA.BROUWTOREN_NAME,
            DBEERPEDIA.BROUWTOREN_FTE, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION).resultSet(
                "Heineken", 80933d, 1864);

    SparqlHttpStub.returnTuple(builder);
  }

  private void init() {
    try {
      server = HttpServer.create(new InetSocketAddress(8900), 0);
      server.createContext("/sparql", new GetSparqlResult(this));
    } catch (Exception exception) {
      fail(exception.getMessage());
    }
  }

  private void startServer() {
    server.start();
  }

  private void stopServer() {
    server.stop(0);
  }

  static class GetSparqlResult implements HttpHandler {

    private SparqlHttpStub parent;

    GetSparqlResult(SparqlHttpStub parent) {
      this.parent = parent;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
      try {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        if (parent.graphResult == null && parent.tupleResultBuilder == null
            && parent.responseCode == 0) {
          fail("Please specify either a graph or tuple result or ok response for your stub.");
        }

        if (parent.graphResult != null) {
          httpExchange.getResponseHeaders().add("Content-Type", MediaTypes.TURTLE);
          Rio.write(parent.graphResult, output, RDFFormat.TURTLE);
        } else if (parent.tupleResultBuilder != null) {
          httpExchange.getResponseHeaders().add("Content-Type", MediaTypes.SPARQL_RESULTS_JSON);
          TupleQueryResultWriter writer = new SPARQLResultsJSONWriter(output);
          QueryResults.report(parent.tupleResultBuilder.build(), writer);
        }

        httpExchange.sendResponseHeaders(parent.responseCode, output.size());
        OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(output.toByteArray());
        responseBody.close();

      } catch (Exception ex) {
        fail(ex.getMessage());
      }
    }
  }

  public static class TupleQueryResultBuilder {

    private String[] bindingNames;

    private List<Object[]> values = new ArrayList<>();

    public TupleQueryResultBuilder(String... bindingNames) {
      this.bindingNames = bindingNames;
    }

    public TupleQueryResultBuilder resultSet(Object... objects) {
      assertThat(objects.length, equalTo(bindingNames.length));
      values.add(objects);
      return this;
    }

    public TupleQueryResult build() {
      MutableTupleQueryResult tupleQueryResult =
          new MutableTupleQueryResult(Arrays.asList(bindingNames));

      values.forEach(valueSet -> {
        QueryBindingSet set = new QueryBindingSet();
        for (int i = 0; i < bindingNames.length; i++) {
          set.addBinding(bindingNames[i], convertValue(valueSet[i]));
        }
        tupleQueryResult.append(set);
      });

      return tupleQueryResult;
    }

    public Value convertValue(Object object) {
      if (object instanceof Value) {
        return (Value) object;
      }

      ValueFactory valueFactory = SimpleValueFactory.getInstance();
      if (object instanceof String) {
        return valueFactory.createLiteral((String) object);
      }
      if (object instanceof Integer) {
        return valueFactory.createLiteral((Integer) object);
      }
      if (object instanceof Double) {
        return valueFactory.createLiteral((Double) object);
      }

      throw new NotSupportedException("Value is not supported: " + object.getClass());
    }
  }

}
