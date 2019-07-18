package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.RepositoryProperties;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeFactory;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.CoreProperties;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResolver;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
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
class Rdf4jConfiguration {

  public static final String LOCAL_REPOSITORY_ID = "local";

  private static final String BASE_DIR_PREFIX = "rdf4j";

  private static final String MODEL_PATH_PATTERN = "model/**";

  @Bean
  public ConfigFactory configFactory() {
    return new ConfigFactoryImpl();
  }

  @Bean
  RepositoryResolver repositoryResolver(@NonNull CoreProperties coreProperties,
      @NonNull Rdf4jProperties rdf4jProperties, @NonNull ConfigFactory configFactory,
      @NonNull ResourceLoader resourceLoader) throws IOException {
    LOG.debug("Initializing repository manager");

    File baseDir = Files.createTempDirectory(BASE_DIR_PREFIX)
        .toFile();
    LocalRepositoryManager repositoryManager = new LocalRepositoryManager(baseDir);
    repositoryManager.initialize();

    // Add & populate local repository
    repositoryManager.addRepositoryConfig(createLocalRepositoryConfig());
    populateLocalRepository(repositoryManager.getRepository(LOCAL_REPOSITORY_ID), resourceLoader,
        coreProperties.getResourcePath());

    // Add repositories from external config
    if (rdf4jProperties.getRepositories() != null) {
      rdf4jProperties.getRepositories()
          .entrySet()
          .stream()
          .map(respositoryProperty -> createRepositoryConfig(respositoryProperty, configFactory))
          .forEach(repositoryManager::addRepositoryConfig);
    }

    return repositoryManager;
  }

  @Bean
  NodeShapeRegistry nodeShapeRegistry(@NonNull List<RepositoryResolver> repositoryResolvers,
      @NonNull Rdf4jProperties rdf4jProperties) {
    Optional<RepositoryResolver> optionalResolver = repositoryResolvers.stream()
        .filter(repositoryResolver -> repositoryResolver.getRepository(LOCAL_REPOSITORY_ID) != null)
        .findFirst();

    if (optionalResolver.isPresent()) {
      RepositoryResolver repositoryResolver = optionalResolver.get();
      Model shapeModel = QueryResults.asModel(repositoryResolver.getRepository(LOCAL_REPOSITORY_ID)
          .getConnection()
          .getStatements(null, null, null, rdf4jProperties.getShape()
              .getGraph()));
      NodeShapeRegistry registry = new NodeShapeRegistry(rdf4jProperties.getShape()
          .getPrefix());

      Models.subjectIRIs(shapeModel.filter(null, RDF.TYPE, SHACL.NODE_SHAPE))
          .stream()
          .map(subject -> NodeShapeFactory.createShapeFromModel(shapeModel, subject))
          .forEach(shape -> registry.register(shape.getIdentifier(), shape));

      return registry;
    }
    throw illegalArgumentException(
        "It is not possible to add a node shape registry to a not existing local repository");
  }

  private static RepositoryConfig createRepositoryConfig(Entry<String, RepositoryProperties> repositoryEntry,
      ConfigFactory configFactory) {
    String repositoryId = repositoryEntry.getKey();
    RepositoryProperties repository = repositoryEntry.getValue();

    RepositoryImplConfig repositoryImplConfig = configFactory.create(repository.getType(),
        repository.getArgs() != null ? repository.getArgs() : ImmutableMap.of());
    repositoryImplConfig.validate();

    return new RepositoryConfig(repositoryId, repositoryImplConfig);
  }

  private static RepositoryConfig createLocalRepositoryConfig() {
    SailRepositoryConfig repositoryConfig = new SailRepositoryConfig(new MemoryStoreConfig());
    return new RepositoryConfig(LOCAL_REPOSITORY_ID, repositoryConfig);
  }

  private static void populateLocalRepository(Repository repository, ResourceLoader resourceLoader, URI resourcePath) {
    Resource[] resourceList;

    try {
      resourceList = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
          .getResources(resourcePath.resolve(MODEL_PATH_PATTERN)
              .toString());
    } catch (IOException e) {
      throw new UncheckedIOException("Error while loading local model.", e);
    }

    @Cleanup
    RepositoryConnection con = repository.getConnection();

    Arrays.stream(resourceList)
        .filter(Resource::isReadable)
        .filter(resource -> resource.getFilename() != null)
        .forEach(modelResource -> {
          String fileExtension = Arrays.stream(modelResource.getFilename()
              .split("\\."))
              .reduce("", (s1, s2) -> s2);

          RDFFormat format = FileFormats.getFormat(fileExtension);

          if (format != null) {
            LOG.debug("Adding '{}' into '{}' repository", modelResource.getFilename(), LOCAL_REPOSITORY_ID);

            try {
              con.add(modelResource.getInputStream(), "", format);
            } catch (IOException e) {
              throw new UncheckedIOException("Error while loading data.", e);
            }
          }
        });
  }

}
