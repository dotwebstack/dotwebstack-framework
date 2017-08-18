package org.dotwebstack.framework.site;

import java.util.HashMap;
import java.util.Optional;
import javax.annotation.PostConstruct;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SiteLoader {
  private static final Logger LOG = LoggerFactory.getLogger(SiteLoader.class);

  private final ConfigurationBackend configurationBackend;
  private HashMap<IRI, Site> sites = new HashMap<>();

  @Autowired
  public SiteLoader(ConfigurationBackend configurationBackend) {
    this.configurationBackend = configurationBackend;
  }

  @PostConstruct
  public void load() {
    Model siteModels = getModelFromConfiguration();

    siteModels.subjects().forEach(identifier -> {
      Model sitesTriples = siteModels.filter(identifier, null, null);
      if (identifier instanceof IRI) {
        IRI iri = (IRI) identifier;
        Site site =
            createSite(iri, sitesTriples);

        sites.put(iri, site);

        LOG.info("Registered site: <{}>", site.getIdentifier());
      }
    });
  }

  public Site getSite(IRI identifier) {
    if (!sites.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Site <%s> not found.", identifier));
    }

    return sites.get(identifier);
  }

  public int getNumberOfSites() {
    return sites.size();
  }

  private Site createSite(IRI identifier, Model statements) {
    Models.objectIRI(statements.filter(identifier, RDF.TYPE, null)).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for site <%s>.", RDF.TYPE, identifier)));

    Site.Builder builder = new Site.Builder(identifier);
    getObjectString(statements, ELMO.DOMAIN).ifPresent(domain -> builder.domain(domain));

    return builder.build();
  }

  private Optional<String> getObjectString(Model sitesTriples, IRI predicate) {
    return Models.objectString(sitesTriples.filter(null, predicate, null));
  }

  private Model getModelFromConfiguration() {
    try (RepositoryConnection conn = configurationBackend.getRepository().getConnection()) {
      String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
      GraphQuery graphQuery = conn.prepareGraphQuery(query);
      graphQuery.setBinding("type", ELMO.SITE);
      return QueryResults.asModel(graphQuery.evaluate());
    }
  }
}
