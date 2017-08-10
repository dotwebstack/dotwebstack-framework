package org.dotwebstack.framework.config;

import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

public class FileConfigurationBackend implements ConfigurationBackend, ResourceLoaderAware {

  private static final Logger LOG = LoggerFactory.getLogger(FileConfigurationBackend.class);

  private SailRepository repository;

  private ResourceLoader resourceLoader;

  @Autowired
  public FileConfigurationBackend() {
    repository = new SailRepository(new MemoryStore());
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = Objects.requireNonNull(resourceLoader);
  }

  @Override
  public void initialize() throws IOException {
    repository.initialize();
    loadResources();
  }

  @Override
  public SailRepository getRepository() {
    if (!repository.isInitialized()) {
      throw new ConfigurationException(
          "Repository cannot be retrieved until it has been initialized.");
    }

    return repository;
  }

  private void loadResources() throws IOException {
    Resource[] resources =
        ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            "classpath:**/model/*");

    if (resources.length == 0) {
      LOG.info("No configuration files found");
      return;
    }

    try (RepositoryConnection conn = repository.getConnection()) {
      for (Resource resource : resources) {
        String extension = FilenameUtils.getExtension(resource.getFilename());

        if (!FileFormats.containsExtension(extension)) {
          LOG.debug("File extension not supported, ignoring file: \"{}\"", resource.getFilename());
          continue;
        }

        conn.add(resource.getInputStream(), "#", FileFormats.getFormat(extension));
        LOG.info("Loaded configuration file: \"{}\"", resource.getFilename());
      }
    }
  }

}
