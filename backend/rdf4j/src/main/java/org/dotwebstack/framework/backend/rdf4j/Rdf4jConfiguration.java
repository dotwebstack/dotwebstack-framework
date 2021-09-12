package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeFactory.createShapeFromModel;
import static org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeFactory.processInheritance;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.EndpointProperties;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.trig.TriGParser;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
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

  private static final String SHAPES_PATH = "shapes";

  private static final String DATA_PATH = "data";

  private static final String FILE_PATTERN = "/**.trig";

  private final Rdf4jProperties rdf4jProperties;

  private final ResourceLoader resourceLoader;

  public Rdf4jConfiguration(Rdf4jProperties rdf4jProperties, ResourceLoader resourceLoader) {
    this.rdf4jProperties = rdf4jProperties;
    this.resourceLoader = resourceLoader;
  }

  @Bean
  Repository repository() {
    return Optional.ofNullable(rdf4jProperties.getEndpoint())
        .map(this::createRemoteRepository)
        .orElseGet(this::createLocalRepository);
  }

  private Repository createRemoteRepository(EndpointProperties endpoint) {
    var repository = new SPARQLRepository(endpoint.getUrl());

    if (endpoint.getUsername() != null && endpoint.getPassword() != null) {
      repository.setUsernameAndPassword(endpoint.getUsername(), endpoint.getPassword());
    }

    if (endpoint.getHeaders() != null) {
      repository.setAdditionalHttpHeaders(endpoint.getHeaders());
    }

    return repository;
  }

  private Repository createLocalRepository() {
    var repository = new SailRepository(new MemoryStore());
    var dataModel = readModel(DATA_PATH);

    try (var conn = repository.getConnection()) {
      conn.add(dataModel);
    }

    return repository;
  }

  @Bean
  NodeShapeRegistry nodeShapeRegistry() {
    var shapeModel = readModel(SHAPES_PATH);

    var registry = new NodeShapeRegistry(rdf4jProperties.getShape()
        .getPrefix());

    Map<org.eclipse.rdf4j.model.Resource, NodeShape> nodeShapeMap = new HashMap<>();

    Models.subjectIRIs(shapeModel.filter(null, RDF.TYPE, SHACL.NODE_SHAPE))
        .forEach(subject -> createShapeFromModel(shapeModel, subject, nodeShapeMap));

    nodeShapeMap.values()
        .forEach(shape -> {
          processInheritance(shape, nodeShapeMap);
          registry.register(shape.getIdentifier(), shape);
        });

    return registry;
  }

  private Model readModel(String path) {
    var model = new LinkedHashModel();
    var parser = new TriGParser().setRDFHandler(new StatementCollector(model));

    findResources(path).forEach(resource -> parse(parser, resource));

    return model;
  }

  @SneakyThrows(IOException.class)
  private List<Resource> findResources(String path) {
    var modelFolder = ResourceLoaderUtils.getResource(path)
        .orElseThrow(() -> new InvalidConfigurationException("Model path not found."));

    var searchPattern = modelFolder.getURI()
        .toString()
        .concat(FILE_PATTERN);

    var resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
        .getResources(searchPattern);

    return Arrays.stream(resources)
        .filter(Resource::isFile)
        .collect(Collectors.toList());
  }

  @SneakyThrows(IOException.class)
  private void parse(RDFParser parser, Resource resource) {
    parser.parse(resource.getInputStream());
  }
}
