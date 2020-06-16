package org.dotwebstack.framework.backend.rdf4j.model;


import java.io.InputStream;
import lombok.Getter;


@Getter
public class SparqlResult {
  private InputStream inputStream;

  public SparqlResult(InputStream source) {
    this.inputStream = source;
  }
}
