package org.dotwebstack.framework;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.informationproduct.InformationProductLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class Application {

  private ConfigurationBackend configurationBackend;

  private BackendLoader backendLoader;

  private InformationProductLoader informationProductLoader;

  @Autowired
  public Application(ConfigurationBackend configurationBackend, BackendLoader backendLoader,
      InformationProductLoader informationProductLoader) {
    this.configurationBackend = configurationBackend;
    this.backendLoader = backendLoader;
    this.informationProductLoader = informationProductLoader;
  }

  @PostConstruct
  public void load() throws IOException {
    configurationBackend.initialize();
    backendLoader.load();
    informationProductLoader.load();
  }

}
