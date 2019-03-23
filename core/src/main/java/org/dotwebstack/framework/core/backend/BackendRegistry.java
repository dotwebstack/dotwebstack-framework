package org.dotwebstack.framework.core.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackendRegistry {

  private final HashMap<String, Backend> backends = new HashMap<>();

  private final Collection<BackendLoader> backendLoaders;

  @PostConstruct
  public void init() throws IOException {
    for (BackendLoader loader : backendLoaders) {
      loader.load(this);
    }
  }

  public void register(String name, Backend backend) {
    backends.put(name, backend);
  }

  public boolean has(String name) {
    return backends.containsKey(name);
  }

  public Backend get(String name) {
    return backends.get(name);
  }

}
