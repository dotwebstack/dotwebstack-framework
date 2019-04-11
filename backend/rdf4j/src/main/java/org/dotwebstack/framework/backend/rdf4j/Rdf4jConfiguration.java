package org.dotwebstack.framework.backend.rdf4j;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.RepositoryProperties;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

@Slf4j
@Configuration
@EnableConfigurationProperties(Rdf4jProperties.class)
public class Rdf4jConfiguration {

  public static final String LOCAL_REPOSITORY_ID = "local";

  private static final String MODEL_PATH_PATTERN = "classpath:config/model/**";

  @Bean
  ConfigFactory configFactory() {
    return new ConfigFactoryImpl();
  }

  @Bean
  RepositoryManager repositoryManager(@NonNull Rdf4jProperties configurationProperties,
      @NonNull ConfigFactory configFactory, @NonNull ResourceLoader resourceLoader) {
    LOG.debug("Initializing repository manager");

    LocalRepositoryManager repositoryManager = new LocalRepositoryManager(
        new File("./.rdf4j/"));
    repositoryManager.init();

    // Add & populate local repository
    repositoryManager.addRepositoryConfig(createLocalRepositoryConfig());
    populateLocalRepository(repositoryManager.getRepository(LOCAL_REPOSITORY_ID), resourceLoader);

    // Add repositories from external config
    if (configurationProperties.getRepositories() != null) {
      configurationProperties.getRepositories()
          .entrySet()
          .stream()
          .map(p -> this.createRepositoryConfig(p, configFactory))
          .forEach(repositoryManager::addRepositoryConfig);
    }

    return repositoryManager;
  }

  @Bean
  NodeShapeRegistry nodeShapeRegistry(@NonNull RepositoryManager repositoryManager,
      @NonNull Rdf4jProperties rdf4jProperties) {
    Model shapeModel = QueryResults.asModel(
        repositoryManager.getRepository(LOCAL_REPOSITORY_ID).getConnection()
            .getStatements(null, null, null, rdf4jProperties.getShape().getGraph()));
    NodeShapeRegistry registry = new NodeShapeRegistry(rdf4jProperties.getShape().getPrefix());

    Models.subjectIRIs(shapeModel.filter(null, RDF.TYPE, SHACL.NODE_SHAPE))
        .stream()
        .map(subject -> NodeShape.fromShapeModel(shapeModel, subject))
        .forEach(shape -> registry.register(shape.getIdentifier(), shape));

    return registry;
  }

  private RepositoryConfig createRepositoryConfig(
      Entry<String, RepositoryProperties> repositoryEntry, ConfigFactory configFactory) {
    String repositoryId = repositoryEntry.getKey();
    RepositoryProperties repository = repositoryEntry.getValue();

    RepositoryImplConfig repositoryImplConfig = configFactory
        .create(repository.getType(),
            repository.getArgs() != null ? repository.getArgs() : ImmutableMap.of());
    repositoryImplConfig.validate();

    return new RepositoryConfig(repositoryId, repositoryImplConfig);
  }

  private RepositoryConfig createLocalRepositoryConfig() {
    SailRepositoryConfig repositoryConfig = new SailRepositoryConfig(new MemoryStoreConfig());
    return new RepositoryConfig(LOCAL_REPOSITORY_ID, repositoryConfig);
  }

  private void populateLocalRepository(Repository repository, ResourceLoader resourceLoader) {
    Resource[] resourceList;

    try {
      resourceList = ResourcePatternUtils
          .getResourcePatternResolver(resourceLoader)
          .getResources(MODEL_PATH_PATTERN);
    } catch (IOException e) {
      throw new InvalidConfigurationException("Error while loading local model.", e);
    }

    @Cleanup RepositoryConnection con = repository.getConnection();

    Arrays.stream(resourceList)
        .filter(Resource::isReadable)
        .filter(resource -> resource.getFilename() != null)
        .forEach(modelResource -> {
          String fileExtension = Arrays
              .stream(modelResource.getFilename().split("\\."))
              .reduce("", (s1, s2) -> s2);

          RDFFormat format = FileFormats.getFormat(fileExtension);

          if (format != null) {
            LOG.debug("Adding '{}' into '{}' repository",
                modelResource.getFilename(), LOCAL_REPOSITORY_ID);

            try {
              con.add(modelResource.getInputStream(), "", format);
            } catch (IOException e) {
              throw new InvalidConfigurationException("Error while loading data.", e);
            }
          }
        });
  }

}
