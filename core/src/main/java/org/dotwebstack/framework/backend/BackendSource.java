package org.dotwebstack.framework.backend;

public interface BackendSource {

  Backend getBackend();

  Object getResult();

  QueryType getQueryType();

}
