package org.dotwebstack.framework.core;

import java.util.HashMap;

public class BackendRegistry {

  private final HashMap<String, Backend> backends = new HashMap<>();

  public void register(String name, Backend backend) {
    backends.put(name, backend);
  }

  public Backend get(String name) {
    return backends.get(name);
  }

}
