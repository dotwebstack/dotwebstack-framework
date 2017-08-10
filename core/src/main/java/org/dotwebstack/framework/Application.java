package org.dotwebstack.framework;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class Application {

  private ConfigurationBackend configurationBackend;

  private BackendLoader backendLoader;

  @Autowired
  public Application(ConfigurationBackend configurationBackend, BackendLoader backendLoader) {
    this.configurationBackend = configurationBackend;
    this.backendLoader = backendLoader;
  }

  @PostConstruct
  public void load() throws IOException {
    configurationBackend.initialize();
    backendLoader.load();
  }

}
