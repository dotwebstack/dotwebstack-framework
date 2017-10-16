package org.dotwebstack.framework.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    final Optional<Resource> optionalPrefixesResource = getPrefixesResource(resources);
    if (optionalPrefixesResource.isPresent()) {
      checkPrefixesResource(optionalPrefixesResource.get());
    }

    try {
      for (Resource resource : resources) {
        String extension = FilenameUtils.getExtension(resource.getFilename());

        if (!FileFormats.containsExtension(extension)) {
          LOG.debug("File extension not supported, ignoring file: \"{}\"", resource.getFilename());
          continue;
        }
        if (optionalPrefixesResource.isPresent()) {
          final SequenceInputStream resourceSquenceInputStream = new SequenceInputStream(
              optionalPrefixesResource.get().getInputStream(), resource.getInputStream());
          repositoryConnection.add(
              new EnvironmentAwareResource(resourceSquenceInputStream, environment)
                  .getInputStream(), "#", FileFormats.getFormat(extension));
        } else {
          repositoryConnection.add(
              new EnvironmentAwareResource(resource.getInputStream(), environment)
                  .getInputStream(), "#", FileFormats.getFormat(extension));
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

  private Optional<Resource> getPrefixesResource(List<Resource> resources) {
    return resources.stream()
        .filter(resource -> resource.getFilename() != null && resource.getFilename()
            .startsWith("_prefixes") && FileFormats
            .containsExtension(FilenameUtils.getExtension(resource.getFilename())))
        .findFirst();
  }

  private void checkPrefixesResource(Resource prefixes) {
    Map<String, String> prefixesMap = new HashMap<>();
    try (BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(prefixes.getFile())))) {
      String line;
      int lineNumber = 0;
      while ((line = bufferedReader.readLine()) != null) {
        lineNumber++;
        String[] parts = line.split(":");
        if (parts.length != 3) {
          throw new ConfigurationException(
              String.format("Found unknown prefix format <%s> at line <%s>", line, lineNumber));
        } else {
          if (!prefixesMap.containsKey(parts[0])) {
            prefixesMap.put(parts[0], parts[1] + ":" + parts[2]);
          } else {
            throw new ConfigurationException(
                String.format("Found multiple declaration <%s> at line <%s>", line, lineNumber));
          }
        }
      }
    } catch (IOException ex) {
      LOG.error("Get error while reading _prefixes.trig --> " + ex.toString());
    }
  }

}
