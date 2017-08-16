package org.dotwebstack.framework;

import org.eclipse.rdf4j.model.IRI;

public class Representation {

  private IRI identifier;
  private InformationProduct informationProduct;
  private String urlPattern;

  // private Stage stage;

  public static class Builder {

    private IRI identifier;
  }
}
