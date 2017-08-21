package org.dotwebstack.framework.site;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SiteResourceProvider
    extends AbstractResourceProvider<Site> {

  @Autowired
  public SiteResourceProvider(ConfigurationBackend configurationBackend) {
    super(configurationBackend);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.SITE);
    return graphQuery;
  }

  @Override
  protected Site createResource(Model model, IRI identifier) {
    Models.objectIRI(model.filter(identifier, RDF.TYPE, null)).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for site <%s>.", RDF.TYPE, identifier)));

    Site.Builder builder = new Site.Builder(identifier);
    getObjectString(model, identifier, ELMO.DOMAIN_PROP).ifPresent(domain -> builder.domain(domain));

    return builder.build();
  }
}
