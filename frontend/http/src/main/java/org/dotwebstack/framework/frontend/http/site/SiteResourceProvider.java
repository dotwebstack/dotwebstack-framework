package org.dotwebstack.framework.frontend.http.site;

import java.util.Optional;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SiteResourceProvider extends AbstractResourceProvider<Site> {

  @Autowired
  public SiteResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
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
    Site.Builder builder = new Site.Builder(identifier);
    Optional<String> domain = getObjectString(model, identifier, ELMO.DOMAIN);

    // Check if domain already exists
    if (domain.isPresent()) {
      if (getAll().entrySet().stream().anyMatch(
          mapSite -> mapSite.getValue().getDomain().equals(domain.get()))) {
        throw new ConfigurationException(
            String.format("Domain <%s> found for multiple sites.", domain.get()));
      }
      builder.domain(domain.get());
    } else {
      if (getAll().entrySet().stream().anyMatch(mapSite -> mapSite.getValue().isMatchAllDomain())) {
        throw new ConfigurationException("Catch all domain found for multiple sites.");
      }
    }

    return builder.build();
  }

}
