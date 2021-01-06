package org.dotwebstack.framework.backend.rdf4j.model;


import java.io.InputStream;
import lombok.Getter;

@Getter
public class SparqlQueryResult {
  private InputStream inputStream;

  public SparqlQueryResult() {}

  public SparqlQueryResult(InputStream source) {
    this.inputStream = source;
  }

  public boolean hasResult() {
    return inputStream != null;
  }
}
