package org.dotwebstack.framework.frontend.http.layout;

import java.util.Collection;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LayoutResourceProvider extends AbstractResourceProvider<Layout> {

  @Autowired
  public LayoutResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.LAYOUT);

    return graphQuery;
  }

  @Override
  protected Layout createResource(Model model, Resource identifier) {
    Layout.Builder builder = new Layout.Builder(identifier);
    Collection<IRI> iris = getPredicateIris(model, identifier);
    for (IRI key : iris) {
      if (!key.equals(RDF.TYPE)) {
        getObjectValue(model, identifier, key).ifPresent(value -> builder.option(key, value));
      }
    }
    return builder.build();
  }
}
