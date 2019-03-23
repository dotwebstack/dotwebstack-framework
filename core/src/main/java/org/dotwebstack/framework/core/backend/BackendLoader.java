package org.dotwebstack.framework.core.backend;

import java.io.IOException;

public interface BackendLoader {

  void load(BackendRegistry backendRegistry) throws IOException;

}
