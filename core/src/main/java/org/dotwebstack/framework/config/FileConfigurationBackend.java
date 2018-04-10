package org.dotwebstack.framework.config;

import static java.util.Comparator.comparing;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.validation.RdfModelTransformer;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
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

  private final ShaclValidator shaclValidator;

  private final String resourcePath;

  private final Resource elmoConfiguration;

  private final Resource elmoShapes;

  private final SailRepository repository;

  private ResourceLoader resourceLoader;

  private Environment environment;

  public FileConfigurationBackend(@NonNull Resource elmoConfiguration,
      @NonNull SailRepository repository, @NonNull String resourcePath,
      @NonNull Resource elmoShapes, @NonNull ShaclValidator shaclValidator) {
    this.elmoConfiguration = elmoConfiguration;
    this.repository = repository;
    this.resourcePath = resourcePath;
    this.elmoShapes = elmoShapes;
    this.shaclValidator = shaclValidator;
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
    optionalPrefixesResource.ifPresent(prefixesResource -> {
      try {
        checkMultiplePrefixesDeclaration(prefixesResource);
      } catch (IOException e) {
        throw new ConfigurationException("Error while reading _prefixes.trig.", e);
      }
    });
    List<InputStream> configurationStreams = new ArrayList<>();
    try {
      resources.sort(comparing(Resource::getFilename));
      for (Resource resource : resources) {
        String extension = FilenameUtils.getExtension(resource.getFilename());
        if (!FileFormats.containsExtension(extension)) {
          LOG.debug("File extension not supported, ignoring file: \"{}\"", resource.getFilename());
          continue;
        }
        addResourceToRepositoryConnection(repositoryConnection, optionalPrefixesResource, resource,
            configurationStreams);
        LOG.info("Loaded configuration file: \"{}\"", resource.getFilename());
      }
      Model model = QueryResults.asModel(
          repositoryConnection.getStatements(null, null, null, ELMO.SHACL_GRAPHNAME));
      validate(configurationStreams, model);
    } catch (RDF4JException e) {
      throw new ConfigurationException("Error while loading RDF data.", e);
    } finally {
      repositoryConnection.close();
    }
  }

  private void addResourceToRepositoryConnection(RepositoryConnection repositoryConnection,
      Optional<Resource> optionalPrefixesResource, Resource resource,
      List<InputStream> streamList) {
    final String extension = FilenameUtils.getExtension(resource.getFilename());
    try {
      final InputStream inputStreamWithEnv;
      if (optionalPrefixesResource.isPresent()
          && !resource.getFilename().equals("_prefixes.trig")) {
        try (SequenceInputStream resourceSequenceInputStream = new SequenceInputStream(
            optionalPrefixesResource.get().getInputStream(), resource.getInputStream())) {
          inputStreamWithEnv = new EnvironmentAwareResource(resourceSequenceInputStream,
              environment).getInputStream();
        }
      } else {
        inputStreamWithEnv =
            new EnvironmentAwareResource(resource.getInputStream(), environment).getInputStream();
      }
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ByteStreams.copy(inputStreamWithEnv, outputStream);
      repositoryConnection.add(new ByteArrayInputStream(outputStream.toByteArray()), "#",
          FileFormats.getFormat(extension));
      streamList.add(new ByteArrayInputStream(outputStream.toByteArray()));
    } catch (IOException ex) {
      LOG.error("Configuration file {} could not be read.", resource.getFilename());
      throw new ConfigurationException(
          String.format("Configuration file <%s> could not be read.", resource.getFilename()));
    }
  }

  private void validate(List<InputStream> configurationStreams, Model model) {
    if (!configurationStreams.isEmpty()) {
      try (InputStream stream =
          new SequenceInputStream(Collections.enumeration(configurationStreams))) {
        final ValidationReport report =
            shaclValidator.validate(RdfModelTransformer.getModel(stream), model);
        if (!report.isValid()) {
          throw new ShaclValidationException(report.printReport());
        }
      } catch (IOException ex) {
        throw new ShaclValidationException("Configuration files could not be read.", ex);
      }
    } else {
      LOG.error("Found no configuration files");
    }
  }

  private List<Resource> getCombinedResources(Resource[] projectResources) {
    List<Resource> result = new ArrayList<>(Arrays.asList(projectResources));
    result.add(elmoConfiguration);
    result.add(elmoShapes);
    return result;
  }

  private Optional<Resource> getPrefixesResource(List<Resource> resources) {
    return resources.stream().filter(resource -> resource.getFilename() != null
        && resource.getFilename().startsWith("_prefixes") && FileFormats.containsExtension(
            FilenameUtils.getExtension(resource.getFilename()))).findFirst();
  }

  private String[] getPrefixesOfResource(Resource inputResource) throws IOException {
    String result =
        CharStreams.toString(new InputStreamReader(inputResource.getInputStream(), Charsets.UTF_8));
    return result.split("\n");
  }


  private void checkMultiplePrefixesDeclaration(Resource prefixes) throws IOException {
    Map<String, String> prefixesMap = new HashMap<>();

    final String[] allPrefixes = getPrefixesOfResource(prefixes);
    int lineNumber = 0;
    for (String prefix : allPrefixes) {
      lineNumber++;
      String[] parts = prefix.split(":");
      if (parts.length != 3) {
        throw new ConfigurationException(
            String.format("Found unknown prefix format <%s> at line <%s>", prefix, lineNumber));
      } else {
        if (!prefixesMap.containsKey(parts[0])) {
          prefixesMap.put(parts[0], parts[1] + ":" + parts[2]);
        } else {
          throw new ConfigurationException(
              String.format("Found multiple declaration <%s> at line <%s>", prefix, lineNumber));
        }
      }
    }
  }

}
