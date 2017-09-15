package org.dotwebstack.framework;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

@SuppressWarnings("restriction")
public class SparqlHttpStub {

  private static SparqlHttpStub instance;

  private HttpServer server;

  private Model model;

  private SparqlHttpStub() {
    init();
  }

  public static void start() {
    if (instance == null) {
      instance = new SparqlHttpStub();
    }
    instance.startServer();
  }

  public static void stop() {
    instance.stopServer();
    instance = null;
  }

  public static void returnModel(Model model) {
    assertThat(instance, is(not(nullValue())));
    instance.model = model;
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
        httpExchange.getResponseHeaders().add("Content-Type", "text/turtle");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertThat(parent.model, is(not(nullValue())));

        Rio.write(parent.model, output, RDFFormat.TURTLE);

        httpExchange.sendResponseHeaders(200, output.size());
        OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(output.toByteArray());
        responseBody.close();

      } catch (Exception ex) {
        fail(ex.getMessage());
      }
    }
  }

  public static void main(String[] args) throws IOException {
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.start();
    SparqlHttpStub.returnModel(model);

    System.out.println("Press enter to exit.");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    br.readLine();

    SparqlHttpStub.stop();
  }

}
