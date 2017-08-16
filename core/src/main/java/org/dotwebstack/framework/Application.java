package org.dotwebstack.framework;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class Application {

  private ConfigurationBackend configurationBackend;

  private BackendLoader backendLoader;

  private List<PostLoadExtension> postLoadExtensions;

  @Autowired
  public Application(ConfigurationBackend configurationBackend, BackendLoader backendLoader,
      List<PostLoadExtension> extensions) {
    this.configurationBackend = Objects.requireNonNull(configurationBackend);
    this.backendLoader = Objects.requireNonNull(backendLoader);
    this.postLoadExtensions = Objects.requireNonNull(extensions);
  }

  @PostConstruct
  public void load() throws IOException {
    configurationBackend.initialize();
    backendLoader.load();
    postLoadExtensions.forEach(PostLoadExtension::postLoad);
  }

}
