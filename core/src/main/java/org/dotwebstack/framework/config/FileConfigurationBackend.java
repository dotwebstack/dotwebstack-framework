package org.dotwebstack.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

public class FileConfigurationBackend implements ConfigurationBackend, ResourceLoaderAware {

  private static final Logger LOG = LoggerFactory.getLogger(FileConfigurationBackend.class);

  private String resourcePath;

  private Resource elmoConfiguration;

  private SailRepository repository;

  private ResourceLoader resourceLoader;

  public FileConfigurationBackend(@NonNull Resource elmoConfiguration, SailRepository repository,
      String resourcePath) {
    this.elmoConfiguration = elmoConfiguration;
    this.repository = repository;
    this.resourcePath = resourcePath;
    repository.initialize();
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public SailRepository getRepository() {
    return repository;
  }

  @PostConstruct
  public void loadResources() throws IOException {
    Resource[] projectResources =
        ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            resourcePath + "/model/**");

    if (projectResources.length == 0) {
      LOG.info("No model configuration files found");
      return;
    }

    RepositoryConnection repositoryConnection;

    try {
      repositoryConnection = repository.getConnection();
    } catch (RepositoryException e) {
      throw new ConfigurationException("Error while getting repository connection.", e);
    }

    List<Resource> resources = getCombinedResources(projectResources);

    try {
      for (Resource resource : resources) {
        String extension = FilenameUtils.getExtension(resource.getFilename());

        if (!FileFormats.containsExtension(extension)) {
          LOG.debug("File extension not supported, ignoring file: \"{}\"", resource.getFilename());
          continue;
        }

        repositoryConnection.add(
            new EnvironmentAwareResource(resource.getInputStream()).getInputStream(), "#",
            FileFormats.getFormat(extension));
        LOG.info("Loaded configuration file: \"{}\"", resource.getFilename());
      }
    } catch (RDF4JException e) {
      throw new ConfigurationException("Error while loading RDF data.", e);
    } finally {
      repositoryConnection.close();
    }
  }

  private List<Resource> getCombinedResources(Resource[] projectResources) {
    List<Resource> result = new ArrayList<>(Arrays.asList(projectResources));
    result.add(elmoConfiguration);
    return result;
  }

}
