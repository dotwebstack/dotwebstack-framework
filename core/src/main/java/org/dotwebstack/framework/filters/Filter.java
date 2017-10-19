package org.dotwebstack.framework.filters;


public interface Filter {

  String filter(String value, String query);

  String getPlaceHolder();
}
