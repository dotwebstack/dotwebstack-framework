package org.dotwebstack.framework;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
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

  private List<PostLoadExtension> postLoadExtensions;

  @Autowired
  public Application(ConfigurationBackend configurationBackend, BackendLoader backendLoader,
      InformationProductLoader informationProductLoader, List<PostLoadExtension> extensions) {
    this.configurationBackend = Objects.requireNonNull(configurationBackend);
    this.backendLoader = Objects.requireNonNull(backendLoader);
    this.informationProductLoader = Objects.requireNonNull(informationProductLoader);
    this.postLoadExtensions = Objects.requireNonNull(extensions);
  }

  @PostConstruct
  public void load() throws IOException {
    configurationBackend.initialize();
    backendLoader.load();
    informationProductLoader.load();
    postLoadExtensions.forEach(PostLoadExtension::postLoad);
  }

}
