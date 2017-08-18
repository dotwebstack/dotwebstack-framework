package org.dotwebstack.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Repository;

@Repository
public class FileConfigurationBackend implements ConfigurationBackend, ResourceLoaderAware {

  private static final Logger LOG = LoggerFactory.getLogger(FileConfigurationBackend.class);

  private final Resource elmoConfiguration;

  private SailRepository repository;

  private ResourceLoader resourceLoader;

  public FileConfigurationBackend(@Value("classpath:/model/elmo.ttl") Resource elmoConfiguration) {
    this.elmoConfiguration = Objects.requireNonNull(elmoConfiguration);
    repository = new SailRepository(new MemoryStore());
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = Objects.requireNonNull(resourceLoader);
  }

  @PostConstruct
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
    Resource[] projectResources =
        ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            "classpath:**/model/*");

   if (projectResources.length == 0) {
      LOG.info("No configuration files found");
      return;
    }

    List<Resource> resources = getCombinedResources(projectResources);

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

  private List<Resource> getCombinedResources(Resource[] projectResources) {
    List<Resource> resources = new ArrayList<>(Arrays.asList(projectResources));
    resources.add(elmoConfiguration);
    return resources;
  }

}
