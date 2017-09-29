package org.dotwebstack.framework.config;

import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

public class FileConfigurationBackend
    implements ConfigurationBackend, ResourceLoaderAware, EnvironmentAware {

  private static final Logger LOG = LoggerFactory.getLogger(FileConfigurationBackend.class);

  private final String resourcePath;

  private final Resource elmoConfiguration;

  private final SailRepository repository;

  private ResourceLoader resourceLoader;

  private Environment environment;

  public FileConfigurationBackend(@NonNull Resource elmoConfiguration,
      @NonNull SailRepository repository, @NonNull String resourcePath) {
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
  public void setEnvironment(@NonNull Environment environment) {
    this.environment = environment;
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
      LOG.warn("No model resources found in path:{}/model", resourcePath);
      return;
    }

    RepositoryConnection repositoryConnection;

    try {
      repositoryConnection = repository.getConnection();
    } catch (RepositoryException e) {
      throw new ConfigurationException("Error while getting repository connection.", e);
    }

    List<Resource> resources = getCombinedResources(projectResources);
    Resource prefixesResource;
    try {
      prefixesResource = getPrefixesResource(resources);
    } catch (IndexOutOfBoundsException ex) {
      LOG.debug("No _prefix file found");
      prefixesResource = null;
    }

    try {
      for (Resource resource : resources) {
        String extension = FilenameUtils.getExtension(resource.getFilename());

        if (!FileFormats.containsExtension(extension)) {
          LOG.debug("File extension not supported, ignoring file: \"{}\"", resource.getFilename());
          continue;
        }

        if (prefixesResource != null) {
          final SequenceInputStream resourceSquenceInputStream = new SequenceInputStream(
              prefixesResource.getInputStream(), resource.getInputStream());
          repositoryConnection.add(
              new EnvironmentAwareResource(resourceSquenceInputStream, environment)
                  .getInputStream(), "#", FileFormats.getFormat(extension));
        } else {
          repositoryConnection.add(
              new EnvironmentAwareResource(resource.getInputStream(), environment).getInputStream(),
              "#", FileFormats.getFormat(extension));
        }
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

  private Resource getPrefixesResource(List<Resource> resources) {
    return resources.stream()
        .filter(resource -> resource.getFilename() != null)
        .filter(resource -> resource.getFilename().startsWith("_"))
        .filter(
            resource -> resource.getFilename().split("\\.")[0].equals("_prefixes") && FileFormats
                .containsExtension(resource.getFilename().split("\\.")[1]))
        .collect(Collectors.toList()).get(0);
  }

}
