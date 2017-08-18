package org.dotwebstack.framework.stage;

import java.util.HashMap;
import java.util.Optional;
import javax.annotation.PostConstruct;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.site.Site;
import org.dotwebstack.framework.site.SiteLoader;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StageLoader {
  private static final Logger LOG = LoggerFactory.getLogger(StageLoader.class);

  private final SiteLoader siteLoader;
  private final ConfigurationBackend configurationBackend;
  private HashMap<IRI, Stage> stages = new HashMap<>();

  @Autowired
  public StageLoader(SiteLoader siteLoader, ConfigurationBackend configurationBackend) {
    this.siteLoader = siteLoader;
    this.configurationBackend = configurationBackend;
  }

  @PostConstruct
  public void load() {
    Model stageModels = getModelFromConfiguration();

    stageModels.subjects().forEach(identifier -> {
      Model stageTriples = stageModels.filter(identifier, null, null);
      if (identifier instanceof IRI) {
        IRI iri = (IRI) identifier;
        Stage stage = createStage(iri, stageTriples);

        stages.put(iri, stage);

        LOG.info("Registered stage: <{}>", stage.getIdentifier());
      }
    });
  }

  public Stage getStage(IRI identifier) {
    if (!stages.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Stage <%s> not found.", identifier));
    }

    return stages.get(identifier);
  }

  public int getNumberOfStages() {
    return stages.size();
  }

  private Stage createStage(IRI identifier, Model statements) {
    IRI siteIri = getIri(statements, ELMO.SITE).orElseThrow(() -> new ConfigurationException(
        String.format("No <%s> site has been found for stage <%s>.", ELMO.SITE, identifier)));

    Stage.Builder builder = new Stage.Builder(identifier, createSite(siteIri));
    getObjectString(statements, ELMO.BASE_PATH).ifPresent(basePath -> builder.basePath(basePath));
    return builder.build();
  }

  private Optional<String> getObjectString(Model stageTriples, IRI predicate) {
    return Models.objectString(stageTriples.filter(null, predicate, null));
  }

  private Optional<IRI> getIri(Model stageTriples, IRI predicate) {
    return Models.objectIRI(stageTriples.filter(null, predicate, null));
  }

  private Site createSite(IRI siteIdentifier) {
    return siteLoader.getSite(siteIdentifier);
  }

  private Model getModelFromConfiguration() {
    try (RepositoryConnection conn = configurationBackend.getRepository().getConnection()) {
      String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
      GraphQuery graphQuery = conn.prepareGraphQuery(query);
      graphQuery.setBinding("type", ELMO.STAGE);
      return QueryResults.asModel(graphQuery.evaluate());
    }
  }
}
