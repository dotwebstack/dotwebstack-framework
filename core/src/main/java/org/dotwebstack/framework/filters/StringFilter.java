package org.dotwebstack.framework.filters;


public class StringFilter implements Filter {

  private String name;

  public StringFilter(String name) {
    this.name = name;
  }

  @Override
  public String filter(String value, String query) {


    // Modify query
    return query;
  }
}
